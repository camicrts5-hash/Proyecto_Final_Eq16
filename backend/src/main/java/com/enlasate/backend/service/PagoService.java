package com.enlasate.backend.service;

import com.enlasate.backend.dto.PagoRequest;
import com.enlasate.backend.dto.PagoResponse;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.PagoTotal;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.PagoTotalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private final PagoTotalRepository pagoRepository;
    private final ClienteRepository clienteRepository;

    public PagoService(PagoTotalRepository pagoRepository,
                       ClienteRepository clienteRepository) {
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<PagoResponse> listar(Pageable pageable) {
        return pagoRepository.findAll(pageable).map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<PagoResponse> listarPorCliente(Integer idCliente, Pageable pageable) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new EntityNotFoundException(
                "No se encontró el cliente con ID: " + idCliente);
        }

        return pagoRepository.findByCliente_IdCliente(idCliente, pageable)
            .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public PagoResponse buscarPorId(Integer id) {
        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id));

        return convertirRespuesta(pago);
    }

    @Transactional
    public PagoResponse registrar(PagoRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()));

        if (pagoRepository.existsByCliente_IdClienteAndMesCorrespondiente(
                request.idCliente(), request.mesCorrespondiente())) {
            throw new IllegalArgumentException(
                "Ya existe un pago registrado para este cliente y mes");
        }

        PagoTotal pago = new PagoTotal();
        pago.setCliente(cliente);
        pago.setMonto(request.monto());
        pago.setMesCorrespondiente(request.mesCorrespondiente());
        pago.setFechaPago(request.fechaPago());
        pago.setMetodoPago(request.metodoPago());

        pago = pagoRepository.save(pago);

        return convertirRespuesta(pago);
    }

    @Transactional
    public PagoResponse actualizar(Integer id, PagoRequest request) {
        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id));

        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()));

        boolean existeOtroPago = pagoRepository
            .existsByCliente_IdClienteAndMesCorrespondiente(
                request.idCliente(), request.mesCorrespondiente());

        if (existeOtroPago
                && !pago.getCliente().getIdCliente().equals(request.idCliente())
                || existeOtroPago
                && !pago.getMesCorrespondiente().equals(request.mesCorrespondiente())) {
            throw new IllegalArgumentException(
                "Ya existe un pago registrado para este cliente y mes");
        }

        pago.setCliente(cliente);
        pago.setMonto(request.monto());
        pago.setMesCorrespondiente(request.mesCorrespondiente());
        pago.setFechaPago(request.fechaPago());
        pago.setMetodoPago(request.metodoPago());

        pago = pagoRepository.save(pago);

        return convertirRespuesta(pago);
    }

    @Transactional
    public void eliminar(Integer id) {
        PagoTotal pago = pagoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el pago con ID: " + id));

        pagoRepository.delete(pago);
    }

    private PagoResponse convertirRespuesta(PagoTotal pago) {
        Cliente cliente = pago.getCliente();

        String nombreCliente = cliente.getUsuario().getNombre()
            + " " + cliente.getUsuario().getApellido();

        return new PagoResponse(
            pago.getIdPago(),
            cliente.getIdCliente(),
            nombreCliente,
            pago.getMonto(),
            pago.getMesCorrespondiente(),
            pago.getFechaPago(),
            pago.getMetodoPago()
        );
    }
}