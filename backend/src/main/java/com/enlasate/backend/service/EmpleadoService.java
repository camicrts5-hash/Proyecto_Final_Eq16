package com.enlasate.backend.service;

import com.enlasate.backend.dto.EmpleadoRequest;
import com.enlasate.backend.dto.EmpleadoResponse;
import com.enlasate.backend.entity.Empleado;
import com.enlasate.backend.entity.Rol;
import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.repository.EmpleadoRepository;
import com.enlasate.backend.repository.RolRepository;
import com.enlasate.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -----------------------------------------------------------------
    // Listar (para el cliente que elige asesor y para el panel)
    // -----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listar() {

        return empleadoRepository.findAll()
                .stream()
                .map(this::convertir)
                .toList();
    }

    // -----------------------------------------------------------------
    // Alta de empleado (solo administrador)
    // -----------------------------------------------------------------
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {

        String correo = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ese correo ya está registrado"
            );
        }

        Rol rol = rolRepository.findByNombreRol("Empleado")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "No se encontró el rol Empleado"
                        )
                );

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

        Empleado empleado = new Empleado();
        empleado.setUsuario(usuario);
        empleado.setPuesto(request.puesto().trim());

        empleado = empleadoRepository.save(empleado);

        return convertir(empleado);
    }

    // -----------------------------------------------------------------
    // Baja de empleado (solo administrador)
    // -----------------------------------------------------------------
    @Transactional
    public void eliminar(Integer id) {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el empleado con ID: " + id
                        )
                );

        Usuario usuario = empleado.getUsuario();

        if (usuario != null) {
            // Al borrar el usuario, el empleado se va en cascada
            // y los clientes que tenía asignados quedan sin asesor
            usuarioRepository.delete(usuario);
        } else {
            empleadoRepository.delete(empleado);
        }
    }

    // -----------------------------------------------------------------
    // Editar empleado (solo administrador)
    // -----------------------------------------------------------------
    @Transactional
    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest request) {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el empleado con ID: " + id
                        )
                );

        Usuario usuario = empleado.getUsuario();

        if (usuario != null) {

            String correo = request.email().trim().toLowerCase();

            if (!usuario.getEmail().equalsIgnoreCase(correo)
                    && usuarioRepository.existsByEmail(correo)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ese correo ya está registrado"
                );
            }

            usuario.setNombre(request.nombre().trim());
            usuario.setApellido(request.apellido() != null ? request.apellido().trim() : "");
            usuario.setEmail(correo);
            usuario.setTelefono(request.telefono().trim());

            usuarioRepository.save(usuario);
        }

        empleado.setPuesto(request.puesto().trim());

        empleado = empleadoRepository.save(empleado);

        return convertir(empleado);
    }

    private EmpleadoResponse convertir(Empleado e) {

        Usuario u = e.getUsuario();

        return new EmpleadoResponse(
                e.getIdEmpleado(),
                u != null ? u.getNombre() + " " + (u.getApellido() != null ? u.getApellido() : "") : "Sin nombre",
                u != null ? u.getEmail() : null,
                u != null ? u.getTelefono() : null,
                e.getPuesto(),
                u != null && u.getRol() != null ? u.getRol().getNombreRol() : "Empleado"
        );
    }
}
