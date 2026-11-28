package com.enlasate.backend.service;

import com.enlasate.backend.dto.ClienteRequest;
import com.enlasate.backend.dto.ClienteResponse;
import com.enlasate.backend.dto.NuevoClienteRequest;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.Empleado;
import com.enlasate.backend.entity.Plan;
import com.enlasate.backend.entity.Rol;
import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.EmpleadoRepository;
import com.enlasate.backend.repository.PlanRepository;
import com.enlasate.backend.repository.RolRepository;
import com.enlasate.backend.repository.UsuarioRepository;
import com.enlasate.backend.security.JwtService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.enlasate.backend.repository.PeriodoServicioRepository periodoRepository;
    private final DomicilioService domicilioService;

    public ClienteService(
            ClienteRepository clienteRepository,
            PlanRepository planRepository,
            UsuarioRepository usuarioRepository,
            EmpleadoRepository empleadoRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            com.enlasate.backend.repository.PeriodoServicioRepository periodoRepository,
            DomicilioService domicilioService) {

        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.periodoRepository = periodoRepository;
        this.domicilioService = domicilioService;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(
            String q, String estado, Boolean conCuenta,
            Integer idEstado, Integer idMunicipio,
            String colonia, Boolean pagoMes, Pageable pageable) {

        String mes = java.time.YearMonth.now().toString();

        return clienteRepository.filtrar(
                (q == null || q.isBlank()) ? null : q.trim(),
                (estado == null || estado.isBlank()) ? null : estado,
                conCuenta,
                idEstado,
                idMunicipio,
                (colonia == null || colonia.isBlank()) ? null : colonia.trim(),
                pagoMes,
                mes,
                pageable
        ).map(this::convertirRespuesta);
    }

    // -----------------------------------------------------------------
    // Clientes agrupados por municipio y colonia
    // -----------------------------------------------------------------
    @Transactional(readOnly = true)
    public java.util.List<com.enlasate.backend.dto.ZonaResponse> porZona() {

        return clienteRepository.agruparPorZona()
                .stream()
                .map(fila -> new com.enlasate.backend.dto.ZonaResponse(
                        fila[0] != null ? fila[0].toString() : "Sin municipio",
                        fila[1] != null ? fila[1].toString() : "Sin colonia",
                        ((Number) fila[2]).longValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        return convertirRespuesta(cliente);
    }

    @Transactional
    public ClienteResponse seleccionarPlan(String email, Integer idPlan) {

        Cliente cliente = clienteRepository.findByUsuario_Email(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró el cliente asociado a esta cuenta"
                        )
                );

        Plan plan = planRepository.findById(idPlan)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El plan seleccionado no existe"
                        )
                );

        cliente.setPlan(plan);

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    @Transactional
    public ClienteResponse actualizar(
            Integer id,
            ClienteRequest request) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        Usuario usuario = cliente.getUsuario();

        if (usuario != null) {

            if (!usuario.getEmail().equalsIgnoreCase(request.email())
                    && usuarioRepository.existsByEmail(request.email())) {

                throw new IllegalArgumentException(
                        "El correo electrónico ya está registrado"
                );
            }

            usuario.setNombre(request.nombre());
            usuario.setApellido(request.apellido());
            usuario.setEmail(request.email());
            usuario.setTelefono(request.telefono());

        } else {

            cliente.setNombreContacto(
                    (request.nombre() + " "
                            + (request.apellido() != null ? request.apellido() : "")).trim()
            );
            cliente.setEmailContacto(request.email());
            cliente.setTelefonoContacto(request.telefono());
        }

        Plan plan = planRepository.findById(request.idPlan())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el plan seleccionado"
                        )
                );

        cliente.setPlan(plan);
        cliente.setEstado(request.estado());

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    private ClienteResponse convertirRespuesta(Cliente cliente) {

        Usuario usuario = cliente.getUsuario();
        Plan plan = cliente.getPlan();

        String nombre = usuario != null ? usuario.getNombre() : cliente.getNombreContacto();
        String email = usuario != null ? usuario.getEmail() : cliente.getEmailContacto();
        String telefono = usuario != null ? usuario.getTelefono() : cliente.getTelefonoContacto();

        return new ClienteResponse(
                cliente.getIdCliente(),
                nombre,
                email,
                telefono,
                plan != null ? plan.getNombrePlan() : null,
                plan != null ? plan.getPrecioMensual() : null,
                cliente.getFechaAlta(),
                cliente.getEstado(),
                cliente.getEmpleado() != null && cliente.getEmpleado().getUsuario() != null
                        ? cliente.getEmpleado().getUsuario().getNombre() + " "
                          + cliente.getEmpleado().getUsuario().getApellido()
                        : null,
                cliente.getCodigoActivacion(),
                cliente.getCodigoExpira(),
                cliente.getFechaInstalacion(),
                usuario != null
        );
    }
    @Transactional(readOnly = true)
public ClienteResponse consultarMiPlan(String email) {

    Cliente cliente = clienteRepository.findByUsuario_Email(email)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No se encontró el cliente asociado a esta cuenta"
                    )
            );

    return convertirRespuesta(cliente);
}

    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        Usuario usuarioCuenta = cliente.getUsuario();

        // Se borra la ficha del cliente primero
        clienteRepository.delete(cliente);

        // Y con ella su acceso: la cuenta queda inactiva y libera el correo
        if (usuarioCuenta != null) {
            usuarioCuenta.setEstado("Inactivo");

            // Se libera el correo para que se pueda volver a registrar
            usuarioCuenta.setEmail(
                    "eliminado_" + usuarioCuenta.getIdUsuario() + "@enlasate.local");

            usuarioRepository.save(usuarioCuenta);
        }
    }

    // -----------------------------------------------------------------
    // El cliente elige al empleado que lo atenderá
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse asignarEmpleado(String email, Integer idEmpleado) {

        Cliente cliente = clienteRepository.findByUsuario_Email(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró el cliente asociado a esta cuenta"
                        )
                );

        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el empleado con ID: " + idEmpleado
                        )
                );

        cliente.setEmpleado(empleado);

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // El cliente activa su propia cuenta con el código que le dio su asesor
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse activarPorCodigo(String email, String codigo) {

        Cliente cliente = clienteRepository.findByUsuario_Email(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró el cliente asociado a esta cuenta"
                        )
                );

        if (!"Pendiente".equalsIgnoreCase(cliente.getEstado())) {
            throw new IllegalArgumentException(
                    "Tu servicio ya está activo"
            );
        }

        if (codigo == null || cliente.getCodigoActivacion() == null
                || !cliente.getCodigoActivacion().equalsIgnoreCase(codigo.trim())) {

            throw new IllegalArgumentException(
                    "El código no coincide con el que te dio tu asesor"
            );
        }

        if (cliente.getCodigoExpira() != null
                && LocalDateTime.now().isAfter(cliente.getCodigoExpira())) {

            throw new IllegalArgumentException(
                    "Tu código venció. Pídele a tu asesor que genere uno nuevo."
            );
        }

        cliente.setEstado("Activo");
        cliente.setFechaInstalacion(LocalDateTime.now());

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    @Transactional
    public ClienteResponse activar(Integer id, String codigo) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        if (!"Pendiente".equalsIgnoreCase(cliente.getEstado())) {
            throw new IllegalArgumentException(
                    "El cliente no está pendiente de instalación (estado actual: "
                            + cliente.getEstado() + ")"
            );
        }

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes capturar el código de activación"
            );
        }

        cliente.setEstado("Activo");
        cliente.setFechaInstalacion(LocalDateTime.now());
        cliente.setCodigoActivacion(codigo.trim().toUpperCase());

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // Alta manual: el empleado o el admin registran al cliente que
    // les escribió por WhatsApp y le generan su código de activación
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse crearManual(NuevoClienteRequest request, String emailEmpleado) {

        String correo = request.email().trim().toLowerCase();
        boolean conCuenta = request.conCuenta() == null || request.conCuenta();

        if (conCuenta && usuarioRepository.existsByEmail(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ese correo ya está registrado"
            );
        }

        Cliente cliente = new Cliente();

        cliente.setFechaAlta(LocalDate.now());
        cliente.setNombreContacto(
                (request.nombre() + " " + (request.apellido() != null ? request.apellido() : "")).trim()
        );
        cliente.setTelefonoContacto(request.telefono().trim());
        cliente.setEmailContacto(correo);

        if (conCuenta) {
            Rol rol = rolRepository.findByNombreRol("Cliente")
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "No se encontró el rol Cliente"
                            )
                    );

            // Nombre de usuario único a partir del correo
            String base = correo.substring(0, correo.indexOf('@'))
                    .replaceAll("[^a-z0-9]", "");
            String nombreUsuario = base;
            int intento = 1;
            while (usuarioRepository.existsByUsuario(nombreUsuario)) {
                nombreUsuario = base + intento++;
            }

            Usuario usuario = new Usuario();
            usuario.setUsuario(nombreUsuario);
            usuario.setPassword(passwordEncoder.encode("Enlasate2026!"));
            usuario.setEmail(correo);
            usuario.setNombre(request.nombre().trim());
            usuario.setApellido(request.apellido() != null ? request.apellido().trim() : "");
            usuario.setTelefono(request.telefono().trim());
            usuario.setEstado("Activo");
            usuario.setRol(rol);

            usuario = usuarioRepository.save(usuario);

            cliente.setUsuario(usuario);
            cliente.setEstado("Pendiente");
            cliente.setCodigoActivacion(generarCodigo());
            cliente.setCodigoExpira(LocalDateTime.now().plusHours(24));

        } else {
            // Sin cuenta: el servicio queda activo desde que el técnico lo instala
            cliente.setUsuario(null);
            cliente.setEstado("Activo");
            cliente.setFechaInstalacion(LocalDateTime.now());
        }

        // Quien lo dio de alta queda como su asesor
        empleadoRepository.findByUsuario_Email(emailEmpleado)
                .ifPresent(cliente::setEmpleado);

        // Plan elegido en el formulario (si lo hay)
        if (request.idPlan() != null) {
            planRepository.findById(request.idPlan())
                    .ifPresent(cliente::setPlan);
        }

        clienteRepository.save(cliente);

        // Domicilio obligatorio del cliente
        domicilioService.registrar(new com.enlasate.backend.dto.DomicilioRequest(
                cliente.getIdCliente(),
                request.idMunicipio(),
                request.calle(),
                request.numero(),
                request.colonia(),
                request.codigoPostal(),
                request.idColonia()
        ));

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // Volver a generar el código de un cliente pendiente (24 h de vigencia)
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse regenerarCodigo(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        if (!"Pendiente".equalsIgnoreCase(cliente.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede generar código a clientes pendientes"
            );
        }

        cliente.setCodigoActivacion(generarCodigo());
        cliente.setCodigoExpira(LocalDateTime.now().plusHours(24));

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // Crear cuenta web para un cliente que se dio de alta sin cuenta
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse crearCuenta(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        if (cliente.getUsuario() != null) {
            throw new IllegalArgumentException(
                    "Este cliente ya tiene cuenta en la aplicación"
            );
        }

        String correo = cliente.getEmailContacto();

        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException(
                    "Primero captura el correo del cliente en sus datos"
            );
        }

        correo = correo.trim().toLowerCase();

        if (usuarioRepository.existsByEmail(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ese correo ya está registrado en otro usuario"
            );
        }

        Rol rol = rolRepository.findByNombreRol("Cliente")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "No se encontró el rol Cliente"
                        )
                );

        String base = correo.substring(0, correo.indexOf('@'))
                .replaceAll("[^a-z0-9]", "");
        String nombreUsuario = base;
        int intento = 1;
        while (usuarioRepository.existsByUsuario(nombreUsuario)) {
            nombreUsuario = base + intento++;
        }

        String completo = cliente.getNombreContacto() != null
                ? cliente.getNombreContacto().trim()
                : "Cliente";

        int espacio = completo.indexOf(' ');
        String nombre = espacio > 0 ? completo.substring(0, espacio) : completo;
        String apellido = espacio > 0 ? completo.substring(espacio + 1) : "";

        Usuario usuario = new Usuario();
        usuario.setUsuario(nombreUsuario);
        usuario.setPassword(passwordEncoder.encode("Enlasate2026!"));
        usuario.setEmail(correo);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setTelefono(cliente.getTelefonoContacto());
        usuario.setEstado("Activo");
        usuario.setRol(rol);

        usuario = usuarioRepository.save(usuario);

        cliente.setUsuario(usuario);
        cliente.setEstado("Pendiente");
        cliente.setFechaInstalacion(null);
        cliente.setCodigoActivacion(generarCodigo());
        cliente.setCodigoExpira(LocalDateTime.now().plusHours(24));

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // Restablecer la contraseña de un cliente (la regresa a la temporal)
    // -----------------------------------------------------------------
    @Transactional
    public void resetPassword(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        if (cliente.getUsuario() == null) {
            throw new IllegalArgumentException(
                    "Este cliente no tiene cuenta en la aplicación"
            );
        }

        Usuario usuario = cliente.getUsuario();
        usuario.setPassword(passwordEncoder.encode("Enlasate2026!"));

        usuarioRepository.save(usuario);
    }

    // -----------------------------------------------------------------
    // Suspender el servicio (cierra el periodo abierto)
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse suspender(Integer id, String motivo) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        com.enlasate.backend.entity.PeriodoServicio periodo = periodoRepository
                .findFirstByCliente_IdClienteAndFechaFinIsNullOrderByFechaInicioDesc(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El cliente no tiene un servicio activo"
                        )
                );

        periodo.setFechaFin(LocalDate.now());
        periodo.setMotivo(motivo != null && !motivo.isBlank()
                ? motivo
                : "Baja del servicio");

        periodoRepository.save(periodo);

        cliente.setEstado("Inactivo");
        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // Reactivar el servicio (abre un periodo nuevo)
    // -----------------------------------------------------------------
    @Transactional
    public ClienteResponse reactivar(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        if (periodoRepository
                .findFirstByCliente_IdClienteAndFechaFinIsNullOrderByFechaInicioDesc(id)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "El servicio de este cliente ya está activo"
            );
        }

        com.enlasate.backend.entity.PeriodoServicio periodo =
                new com.enlasate.backend.entity.PeriodoServicio();

        periodo.setCliente(cliente);
        periodo.setFechaInicio(LocalDate.now());
        periodo.setMotivo("Reactivación del servicio");

        periodoRepository.save(periodo);

        cliente.setEstado("Activo");
        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    // -----------------------------------------------------------------
    // Domicilio del cliente autenticado
    // -----------------------------------------------------------------
    @Transactional(readOnly = true)
    public java.util.List<com.enlasate.backend.dto.DomicilioResponse> miDomicilio(String email) {

        return clienteRepository.findByUsuario_Email(email)
                .map(c -> domicilioService.buscarPorCliente(c.getIdCliente()))
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el cliente asociado a esta cuenta"));
    }

    @Transactional
    public void guardarMiDomicilio(
            String email,
            com.enlasate.backend.dto.DomicilioRequest request) {

        Cliente cliente = clienteRepository.findByUsuario_Email(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el cliente asociado a esta cuenta"));

        var existentes = domicilioService.buscarPorCliente(cliente.getIdCliente());

        var datos = new com.enlasate.backend.dto.DomicilioRequest(
                cliente.getIdCliente(),
                request.idMunicipio(),
                request.calle(),
                request.numero(),
                request.colonia(),
                request.codigoPostal(),
                request.idColonia()
        );

        if (existentes.isEmpty()) {
            domicilioService.registrar(datos);
        } else {
            domicilioService.actualizar(existentes.get(0).idDomicilio(), datos);
        }
    }

    private String generarCodigo() {
        return "ENL-" + String.format("%04d", (int) (Math.random() * 9999));
    }
}