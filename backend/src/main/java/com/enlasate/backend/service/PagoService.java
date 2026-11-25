package com.enlasate.backend.service;

import com.enlasate.backend.dto.EstadoCuentaResponse;
import com.enlasate.backend.dto.PagoRequest;
import com.enlasate.backend.dto.PagoResponse;
import com.enlasate.backend.dto.PagoSimuladoRequest;
import com.enlasate.backend.entity.AuditoriaPago;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.PagoTotal;
import com.enlasate.backend.entity.Plan;
import com.enlasate.backend.repository.AuditoriaPagoRepository;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.PagoTotalRepository;
import com.enlasate.backend.repository.PlanRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class PagoService {

    private final PagoTotalRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;
    private final AuditoriaPagoRepository auditoriaRepository;
    private final com.enlasate.backend.repository.PeriodoServicioRepository periodoRepository;

    public PagoService(
        PagoTotalRepository pagoRepository,
        ClienteRepository clienteRepository,
        PlanRepository planRepository,
        AuditoriaPagoRepository auditoriaRepository,
        com.enlasate.backend.repository.PeriodoServicioRepository periodoRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.periodoRepository = periodoRepository;
    }

    // -----------------------------------------------------------------
    // Estado de cuenta de un cliente: los 12 meses de un año
    // -----------------------------------------------------------------
    @Transactional(readOnly = true)
    public EstadoCuentaResponse estadoCuenta(Integer idCliente, Integer anio) {

        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + idCliente
            ));

        int anioActual = java.time.Year.now().getValue();

        int anioPedido = (anio == null || anio < 2000 || anio > anioActual + 1)
            ? anioActual
            : anio;

        List<PagoTotal> pagos = pagoRepository
            .findByCliente_IdClienteOrderByMesCorrespondienteAsc(idCliente);

        // Años que puede elegir: 3 anteriores, el actual y el siguiente
        int anioAlta = cliente.getFechaAlta() != null
            ? cliente.getFechaAlta().getYear()
            : anioActual;

        int desde = Math.min(anioAlta, anioActual - 3);

        List<Integer> anios = new ArrayList<>();

        for (int a = anioActual + 1; a >= desde; a--) {
            anios.add(a);
        }

        if (!anios.contains(anioPedido)) {
            anios.add(anioPedido);
        }

        anios.sort(Collections.reverseOrder());

        String[] nombres = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };

        BigDecimal total = BigDecimal.ZERO;
        int mesesPagados = 0;

        // Periodos reales de servicio (altas, bajas y reactivaciones)
        List<com.enlasate.backend.entity.PeriodoServicio> periodos =
            periodoRepository.findByCliente_IdClienteOrderByFechaInicioAsc(idCliente);

        // Si el cliente no tiene periodos registrados, se usa su instalación
        java.time.LocalDate inicioServicio = cliente.getFechaInstalacion() != null
            ? cliente.getFechaInstalacion().toLocalDate()
            : cliente.getFechaAlta();

        List<EstadoCuentaResponse.MesEstado> meses = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {

            String clave = String.format("%d-%02d", anioPedido, m);

            java.time.YearMonth mesActual = java.time.YearMonth.of(anioPedido, m);

            boolean aplica;

            if (!periodos.isEmpty()) {
                aplica = periodos.stream().anyMatch(p -> {
                    java.time.YearMonth ini = java.time.YearMonth.from(p.getFechaInicio());
                    java.time.YearMonth fin = p.getFechaFin() != null
                        ? java.time.YearMonth.from(p.getFechaFin())
                        : null;

                    return !mesActual.isBefore(ini)
                        && (fin == null || !mesActual.isAfter(fin));
                });
            } else {
                aplica = inicioServicio == null
                    || !mesActual.isBefore(java.time.YearMonth.from(inicioServicio));
            }

            PagoTotal confirmado = null;
            PagoTotal enRevision = null;

            for (PagoTotal p : pagos) {
                if (clave.equals(p.getMesCorrespondiente())) {
                    if ("Completado".equalsIgnoreCase(p.getEstadoPago())) {
                        confirmado = p;
                    } else {
                        enRevision = p;
                    }
                }
            }

            if (confirmado != null) {
                total = total.add(
                    confirmado.getMonto() != null ? confirmado.getMonto() : BigDecimal.ZERO
                );
                mesesPagados++;

                meses.add(new EstadoCuentaResponse.MesEstado(
                    m, nombres[m - 1], true, true, false,
                    confirmado.getIdPago(),
                    confirmado.getMonto(),
                    confirmado.getFechaPago(),
                    confirmado.getMetodoPago()
                ));
            } else if (enRevision != null) {
                meses.add(new EstadoCuentaResponse.MesEstado(
                    m, nombres[m - 1], false, true, true,
                    enRevision.getIdPago(),
                    enRevision.getMonto(),
                    enRevision.getFechaPago(),
                    enRevision.getMetodoPago()
                ));
            } else {
                meses.add(new EstadoCuentaResponse.MesEstado(
                    m, nombres[m - 1], false, aplica, false, null, null, null, null
                ));
            }
        }

        com.enlasate.backend.entity.Usuario u = cliente.getUsuario();

        boolean servicioActivo = !periodos.isEmpty()
            ? periodos.stream().anyMatch(p -> p.getFechaFin() == null)
            : "Activo".equalsIgnoreCase(cliente.getEstado())
              || "Moroso".equalsIgnoreCase(cliente.getEstado());

        java.time.LocalDate inicioReal = !periodos.isEmpty()
            ? periodos.get(0).getFechaInicio()
            : (cliente.getFechaInstalacion() != null
                ? cliente.getFechaInstalacion().toLocalDate()
                : cliente.getFechaAlta());

        return new EstadoCuentaResponse(
            cliente.getIdCliente(),
            u != null ? u.getNombre() : cliente.getNombreContacto(),
            u != null ? u.getEmail() : cliente.getEmailContacto(),
            u != null ? u.getTelefono() : cliente.getTelefonoContacto(),
            cliente.getPlan() != null ? cliente.getPlan().getNombrePlan() : null,
            cliente.getPlan() != null ? cliente.getPlan().getIdPlan() : null,
            cliente.getPlan() != null ? cliente.getPlan().getPrecioMensual() : null,
            cliente.getEstado(),
            cliente.getEmpleado() != null && cliente.getEmpleado().getUsuario() != null
                ? cliente.getEmpleado().getUsuario().getNombre() : null,
            cliente.getFechaAlta(),
            cliente.getFechaInstalacion(),
            anioPedido,
            anios,
            meses,
            total,
            mesesPagados,
            servicioActivo,
            inicioReal
        );
    }

    // -----------------------------------------------------------------
    // Confirmar un pago pendiente (empleado o admin)
    // -----------------------------------------------------------------
    @Transactional
    public PagoResponse confirmarPago(Integer id) {

        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id
            ));

        if ("Completado".equalsIgnoreCase(pago.getEstadoPago())) {
            throw new IllegalArgumentException(
                "Ese pago ya estaba confirmado"
            );
        }

        pago.setEstadoPago("Completado");

        pagoRepository.save(pago);

        return convertirRespuesta(pago);
    }

    // Listar todos los pagos (ADMIN y EMPLEADO)
    @Transactional(readOnly = true)
    public Page<PagoResponse> listar(Pageable pageable) {

        return pagoRepository.findAll(pageable)
            .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Integer idClientePorEmail(String email) {

        return clienteRepository.findByUsuario_Email(email)
            .map(Cliente::getIdCliente)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente asociado a esta cuenta"
            ));
    }

    // Historial de un cliente por ID (ADMIN y EMPLEADO)
    @Transactional(readOnly = true)
    public Page<PagoResponse> listarPorCliente(
        Integer idCliente,
        Pageable pageable
    ) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new EntityNotFoundException(
                "No se encontró el cliente con ID: " + idCliente
            );
        }

        return pagoRepository
            .findByCliente_IdCliente(idCliente, pageable)
            .map(this::convertirRespuesta);
    }

    // Historial del cliente que inició sesión
    @Transactional(readOnly = true)
    public Page<PagoResponse> listarPorEmail(
        String email,
        Pageable pageable
    ) {
        return pagoRepository
            .findByCliente_Usuario_Email(email, pageable)
            .map(this::convertirRespuesta);
    }

    // Buscar pago por ID
    @Transactional(readOnly = true)
    public PagoResponse buscarPorId(Integer id) {
        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id
            ));

        return convertirRespuesta(pago);
    }

    // Registrar pago simulado del cliente autenticado
    @Transactional
    public PagoResponse registrarPagoSimulado(
        String email,
        PagoSimuladoRequest request
    ) {
        Cliente cliente = clienteRepository.findByUsuario_Email(email)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente asociado a esta cuenta"
            ));

        // El plan lo asigna el asesor: aquí solo se cobra el que ya tiene
        Plan plan = cliente.getPlan();

        if (plan == null) {
            throw new IllegalArgumentException(
                "Todavía no tienes un plan asignado. Contacta a tu asesor para que te lo asigne."
            );
        }

        YearMonth periodo = YearMonth.now();

        String mes = (request.mes() != null && !request.mes().isBlank())
            ? request.mes()
            : periodo.toString();

        if (pagoRepository.existsByCliente_IdClienteAndMesCorrespondiente(
            cliente.getIdCliente(),
            mes
        )) {
            throw new IllegalArgumentException(
                "Ya tienes un pago registrado para el mes " + mes
            );
        }

        String metodo = convertirMetodoPago(request.metodoPago());

        // -----------------------------------------------------------------
        // Efectivo y depósito solo los registra un empleado o el admin.
        // La transferencia la registra el cliente pero queda PENDIENTE
        // hasta que un empleado la confirme.
        // -----------------------------------------------------------------
        if (!"Tarjeta".equalsIgnoreCase(metodo)
                && !"Transferencia".equalsIgnoreCase(metodo)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo puedes pagar con tarjeta o transferencia. "
                            + "Para efectivo o depósito acude con un empleado."
            );
        }

        PagoTotal pago = new PagoTotal();

        pago.setCliente(cliente);
        pago.setPlan(plan);
        pago.setMonto(plan.getPrecioMensual());
        pago.setMesCorrespondiente(mes);
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(metodo);

        // Tarjeta = cobro inmediato · Transferencia = pendiente de revisión
        pago.setEstadoPago(
            "Tarjeta".equalsIgnoreCase(metodo) ? "Completado" : "Pendiente"
        );

        // Guardar el pago
        pago = pagoRepository.save(pago);

        // El plan del cliente NO se modifica aquí (solo lo cambia el asesor)

        // Registrar auditoría
        AuditoriaPago auditoria = new AuditoriaPago();

        auditoria.setIdPago(pago.getIdPago());
        auditoria.setAccion("INSERT");
        auditoria.setMontoAnterior(null);
        auditoria.setMontoNuevo(pago.getMonto());
        auditoria.setUsuarioBd(
            email.length() > 50 ? email.substring(0, 50) : email
        );
        auditoria.setFechaEvento(LocalDateTime.now());

        auditoriaRepository.save(auditoria);

        return convertirRespuesta(pago);
    }

    // Registrar pago manualmente (ADMIN)
    @Transactional
    public PagoResponse registrar(PagoRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()
            ));

        if (pagoRepository.existsByCliente_IdClienteAndMesCorrespondiente(
            request.idCliente(),
            request.mesCorrespondiente()
        )) {
            throw new IllegalArgumentException(
                "Ya existe un pago registrado para este cliente y mes"
            );
        }

        PagoTotal pago = new PagoTotal();

        pago.setCliente(cliente);
        pago.setPlan(cliente.getPlan());
        pago.setMonto(request.monto());
        pago.setMesCorrespondiente(request.mesCorrespondiente());
        pago.setFechaPago(request.fechaPago());
        pago.setMetodoPago(request.metodoPago());
        pago.setEstadoPago("Completado");

        return convertirRespuesta(pagoRepository.save(pago));
    }

    // Actualizar pago (ADMIN)
    @Transactional
    public PagoResponse actualizar(Integer id, PagoRequest request) {
        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id
            ));

        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()
            ));

        boolean existeOtroPago =
            pagoRepository.existsByCliente_IdClienteAndMesCorrespondiente(
                request.idCliente(),
                request.mesCorrespondiente()
            );

        boolean esElMismoPago =
            Objects.equals(
                pago.getCliente().getIdCliente(),
                request.idCliente()
            )
            && Objects.equals(
                pago.getMesCorrespondiente(),
                request.mesCorrespondiente()
            );

        if (existeOtroPago && !esElMismoPago) {
            throw new IllegalArgumentException(
                "Ya existe un pago registrado para este cliente y mes"
            );
        }

        pago.setCliente(cliente);
        pago.setPlan(cliente.getPlan());
        pago.setMonto(request.monto());
        pago.setMesCorrespondiente(request.mesCorrespondiente());
        pago.setFechaPago(request.fechaPago());
        pago.setMetodoPago(request.metodoPago());

        return convertirRespuesta(pagoRepository.save(pago));
    }

    // Eliminar pago (ADMIN)
    @Transactional
    public void eliminar(Integer id) {
        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id
            ));

        pagoRepository.delete(pago);
    }

    // Convertir método de pago a los valores permitidos por MySQL
    private String convertirMetodoPago(String metodo) {
        return switch (metodo.toLowerCase()) {
            case "tarjeta" -> "Tarjeta";
            case "transferencia" -> "Transferencia";
            case "efectivo" -> "Efectivo";
            default -> throw new IllegalArgumentException(
                "Método de pago no válido"
            );
        };
    }

    // Convertir entidad a respuesta
    private PagoResponse convertirRespuesta(PagoTotal pago) {
        Cliente cliente = pago.getCliente();

        String nombre = cliente.getUsuario().getNombre();

        String apellido = cliente.getUsuario().getApellido();

        String nombreCompleto = nombre
            + (apellido == null || apellido.isBlank()
                ? ""
                : " " + apellido);

        String nombrePlan = pago.getPlan() != null
            ? pago.getPlan().getNombrePlan()
            : null;

        return new PagoResponse(
            pago.getIdPago(),
            cliente.getIdCliente(),
            nombreCompleto,
            pago.getMonto(),
            pago.getMesCorrespondiente(),
            pago.getFechaPago(),
            pago.getMetodoPago(),
            nombrePlan,
            pago.getEstadoPago()
        );
    }
}