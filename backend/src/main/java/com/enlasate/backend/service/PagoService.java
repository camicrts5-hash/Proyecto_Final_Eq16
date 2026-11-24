package com.enlasate.backend.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

@Service
public class PagoService {

    private final PagoTotalRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;
    private final AuditoriaPagoRepository auditoriaRepository;

    public PagoService(
        PagoTotalRepository pagoRepository,
        ClienteRepository clienteRepository,
        PlanRepository planRepository,
        AuditoriaPagoRepository auditoriaRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    // Listar todos los pagos (ADMIN y EMPLEADO)
    @Transactional(readOnly = true)
    public Page<PagoResponse> listar(Pageable pageable) {
        return pagoRepository.findAll(pageable)
            .map(this::convertirRespuesta);
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

        Plan plan = planRepository.findById(request.idPlan())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el plan seleccionado"
            ));

        YearMonth periodo = YearMonth.now();
        String mes = periodo.toString();

        if (pagoRepository.existsByCliente_IdClienteAndMesCorrespondiente(
            cliente.getIdCliente(),
            mes
        )) {
            throw new IllegalArgumentException(
                "Ya tienes un pago registrado para el mes " + mes
            );
        }

        String metodo = convertirMetodoPago(request.metodoPago());

        PagoTotal pago = new PagoTotal();

        pago.setCliente(cliente);
        pago.setPlan(plan);
        pago.setMonto(plan.getPrecioMensual());
        pago.setMesCorrespondiente(mes);
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(metodo);
        pago.setEstadoPago("Completado");

        // Guardar el pago
        pago = pagoRepository.save(pago);

        // Actualizar el plan del cliente
        cliente.setPlan(plan);
        clienteRepository.save(cliente);

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