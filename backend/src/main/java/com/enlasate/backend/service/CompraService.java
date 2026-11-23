package com.enlasate.backend.service;

import com.enlasate.backend.dto.CompraRequest;
import com.enlasate.backend.dto.CompraResponse;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.CompraEquipo;
import com.enlasate.backend.entity.Equipo;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.CompraEquipoRepository;
import com.enlasate.backend.repository.EquipoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraService {

    private final CompraEquipoRepository compraRepository;
    private final ClienteRepository clienteRepository;
    private final EquipoRepository equipoRepository;

    public CompraService(
            CompraEquipoRepository compraRepository,
            ClienteRepository clienteRepository,
            EquipoRepository equipoRepository) {
        this.compraRepository = compraRepository;
        this.clienteRepository = clienteRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public Page<CompraResponse> listar(Pageable pageable) {
        return compraRepository.findAll(pageable)
            .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public CompraResponse buscarPorId(Integer id) {
        CompraEquipo compra = compraRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró la compra con ID: " + id));

        return convertirRespuesta(compra);
    }

    @Transactional(readOnly = true)
    public CompraResponse buscarPorCliente(Integer idCliente) {
        CompraEquipo compra = compraRepository
            .findByCliente_IdCliente(idCliente)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró una compra para el cliente con ID: " + idCliente));

        return convertirRespuesta(compra);
    }

    @Transactional
    public CompraResponse registrar(CompraRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()));

        Equipo equipo = equipoRepository.findById(request.idEquipo())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el equipo con ID: " + request.idEquipo()));

        if (compraRepository.findByCliente_IdCliente(request.idCliente()).isPresent()) {
            throw new IllegalArgumentException(
                "El cliente ya tiene una compra registrada");
        }

        CompraEquipo compra = new CompraEquipo();
        compra.setCliente(cliente);
        compra.setEquipo(equipo);
        compra.setPrecioEquipo(request.precioEquipo());
        compra.setFechaCompra(request.fechaCompra());

        compra = compraRepository.save(compra);

        return convertirRespuesta(compra);
    }

    @Transactional
    public CompraResponse actualizar(Integer id, CompraRequest request) {
        CompraEquipo compra = compraRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró la compra con ID: " + id));

        Cliente cliente = clienteRepository.findById(request.idCliente())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el cliente con ID: " + request.idCliente()));

        Equipo equipo = equipoRepository.findById(request.idEquipo())
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró el equipo con ID: " + request.idEquipo()));

        CompraEquipo otraCompra = compraRepository
            .findByCliente_IdCliente(request.idCliente())
            .orElse(null);

        if (otraCompra != null
                && !otraCompra.getIdCompra().equals(id)) {
            throw new IllegalArgumentException(
                "El cliente ya tiene otra compra registrada");
        }

        compra.setCliente(cliente);
        compra.setEquipo(equipo);
        compra.setPrecioEquipo(request.precioEquipo());
        compra.setFechaCompra(request.fechaCompra());

        compra = compraRepository.save(compra);

        return convertirRespuesta(compra);
    }

    @Transactional
    public void eliminar(Integer id) {
        CompraEquipo compra = compraRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "No se encontró la compra con ID: " + id));

        compraRepository.delete(compra);
    }

    private CompraResponse convertirRespuesta(CompraEquipo compra) {
        Cliente cliente = compra.getCliente();
        Equipo equipo = compra.getEquipo();

        String nombreCliente = cliente.getUsuario().getNombre()
            + " " + cliente.getUsuario().getApellido();

        return new CompraResponse(
            compra.getIdCompra(),
            cliente.getIdCliente(),
            nombreCliente,
            equipo.getIdEquipo(),
            equipo.getModelo(),
            equipo.getMarca(),
            compra.getPrecioEquipo(),
            compra.getFechaCompra()
        );
    }
}