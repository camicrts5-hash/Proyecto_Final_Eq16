package com.enlasate.backend.service;

import com.enlasate.backend.dto.SoporteRequest;
import com.enlasate.backend.dto.SoporteResponse;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.Empleado;
import com.enlasate.backend.entity.SoporteTecnico;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.EmpleadoRepository;
import com.enlasate.backend.repository.SoporteTecnicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SoporteService {

    private final SoporteTecnicoRepository soporteRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    public SoporteService(
            SoporteTecnicoRepository soporteRepository,
            ClienteRepository clienteRepository,
            EmpleadoRepository empleadoRepository) {
        this.soporteRepository = soporteRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public Page<SoporteResponse> listar(Pageable pageable) {
        return soporteRepository.findAll(pageable)
            .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<SoporteResponse> listarPorCliente(
            Integer idCliente, Pageable pageable) {
        return soporteRepository.findByCliente_IdCliente(idCliente, pageable)
            .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<SoporteResponse> listarPorEmpleado(
            Integer idEmpleado, Pageable pageable) {
        return soporteRepository.findByEmpleado_IdEmpleado(idEmpleado, pageable)
            .map(this::convertirRespuesta);
    }

    // -----------------------------------------------------------------
    // El cliente autenticado reporta una falla
    // -----------------------------------------------------------------
    @Transactional
    public SoporteResponse registrarPorCliente(String email, String descripcion) {

        Cliente cliente = clienteRepository.findByUsuario_Email(email)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente asociado a esta cuenta"));

        SoporteTecnico ticket = new SoporteTecnico();

        ticket.setCliente(cliente);
        // Se asigna solo a su asesor (puede ser null si no tiene)
        ticket.setEmpleado(cliente.getEmpleado());
        ticket.setDescripcionFalla(descripcion.trim());
        ticket.setFechaReporte(LocalDateTime.now());
        ticket.setEstadoTicket("Abierto");

        return convertirRespuesta(soporteRepository.save(ticket));
    }

    // -----------------------------------------------------------------
    // Tickets del cliente autenticado
    // -----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<SoporteResponse> listarPorEmail(String email, Pageable pageable) {

        Cliente cliente = clienteRepository.findByUsuario_Email(email)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente asociado a esta cuenta"));

        return soporteRepository.findByCliente_IdCliente(cliente.getIdCliente(), pageable)
            .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public SoporteResponse buscarPorId(Integer id) {
        SoporteTecnico ticket = soporteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el ticket con ID: " + id));

        return convertirRespuesta(ticket);
    }

    @Transactional
    public SoporteResponse registrar(SoporteRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()));

        Empleado empleado = empleadoRepository.findById(request.idEmpleado())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el empleado con ID: " + request.idEmpleado()));

        validarFechas(request);

        SoporteTecnico ticket = new SoporteTecnico();
        ticket.setCliente(cliente);
        ticket.setEmpleado(empleado);
        ticket.setDescripcionFalla(request.descripcionFalla());
        ticket.setFechaReporte(request.fechaReporte());
        ticket.setEstadoTicket(request.estadoTicket());
        ticket.setFechaCierre(request.fechaCierre());

        ticket = soporteRepository.save(ticket);

        return convertirRespuesta(ticket);
    }

    @Transactional
    public SoporteResponse actualizar(Integer id, SoporteRequest request) {
        SoporteTecnico ticket = soporteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el ticket con ID: " + id));

        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()));

        Empleado empleado = empleadoRepository.findById(request.idEmpleado())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el empleado con ID: " + request.idEmpleado()));

        validarFechas(request);

        ticket.setCliente(cliente);
        ticket.setEmpleado(empleado);
        ticket.setDescripcionFalla(request.descripcionFalla());
        ticket.setFechaReporte(request.fechaReporte());
        ticket.setEstadoTicket(request.estadoTicket());
        ticket.setFechaCierre(request.fechaCierre());

        ticket = soporteRepository.save(ticket);

        return convertirRespuesta(ticket);
    }

    @Transactional
    public void eliminar(Integer id) {
        SoporteTecnico ticket = soporteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el ticket con ID: " + id));

        soporteRepository.delete(ticket);
    }

    private void validarFechas(SoporteRequest request) {
        if (request.fechaCierre() != null
                && request.fechaCierre().isBefore(request.fechaReporte())) {
            throw new IllegalArgumentException(
                "La fecha de cierre no puede ser anterior a la fecha de reporte");
        }

        if ("Cerrado".equals(request.estadoTicket())
                && request.fechaCierre() == null) {
            throw new IllegalArgumentException(
                "Un ticket cerrado debe tener fecha de cierre");
        }

        if (!"Cerrado".equals(request.estadoTicket())
                && request.fechaCierre() != null) {
            throw new IllegalArgumentException(
                "Solo los tickets cerrados pueden tener fecha de cierre");
        }
    }

    private SoporteResponse convertirRespuesta(SoporteTecnico ticket) {
        Cliente cliente = ticket.getCliente();
        Empleado empleado = ticket.getEmpleado();

        String nombreCliente = cliente != null && cliente.getUsuario() != null
            ? (cliente.getUsuario().getNombre() + " "
                + (cliente.getUsuario().getApellido() != null ? cliente.getUsuario().getApellido() : "")).trim()
            : (cliente != null ? cliente.getNombreContacto() : "Sin cliente");

        String nombreEmpleado = empleado != null && empleado.getUsuario() != null
            ? (empleado.getUsuario().getNombre() + " "
                + (empleado.getUsuario().getApellido() != null ? empleado.getUsuario().getApellido() : "")).trim()
            : "Sin asignar";

        return new SoporteResponse(
            ticket.getIdTicket(),
            cliente.getIdCliente(),
            nombreCliente,
            empleado.getIdEmpleado(),
            nombreEmpleado,
            ticket.getDescripcionFalla(),
            ticket.getFechaReporte(),
            ticket.getEstadoTicket(),
            ticket.getFechaCierre()
        );
    }
}