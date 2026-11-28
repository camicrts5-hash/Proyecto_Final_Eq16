package com.enlasate.backend.service;

import com.enlasate.backend.dto.RentaRequest;
import com.enlasate.backend.dto.RentaResponse;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.Equipo;
import com.enlasate.backend.entity.RentaEquipo;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.EquipoRepository;
import com.enlasate.backend.repository.RentaEquipoRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class RentaService {

    private final RentaEquipoRepository rentaRepository;
    private final ClienteRepository clienteRepository;
    private final EquipoRepository equipoRepository;

    public RentaService(
            RentaEquipoRepository rentaRepository,
            ClienteRepository clienteRepository,
            EquipoRepository equipoRepository) {
        this.rentaRepository = rentaRepository;
        this.clienteRepository = clienteRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public Page<RentaResponse> listar(Pageable pageable) {
        return rentaRepository.findAll(pageable).map(this::convertirResponse);
    }

    @Transactional(readOnly = true)
    public RentaResponse buscarPorId(Integer id) {
        RentaEquipo renta = rentaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró la renta con ID: " + id));

        return convertirResponse(renta);
    }

    @Transactional(readOnly = true)
    public RentaResponse buscarPorCliente(Integer idCliente) {
        RentaEquipo renta = rentaRepository.findByCliente_IdCliente(idCliente)
                .orElseThrow(() ->
                        new EntityNotFoundException("El cliente no tiene una renta registrada"));

        return convertirResponse(renta);
    }

    public RentaResponse registrar(RentaRequest request) {

        validarFechas(request);

        if (rentaRepository.findByCliente_IdCliente(request.idCliente()).isPresent()) {
            throw new IllegalArgumentException(
                    "El cliente ya tiene una renta registrada");
        }

        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró el cliente"));

        Equipo equipo = equipoRepository.findById(request.idEquipo())
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró el equipo"));

        RentaEquipo renta = new RentaEquipo();
        renta.setCliente(cliente);
        renta.setEquipo(equipo);
        renta.setRentaMensualEquipo(request.rentaMensualEquipo());
        renta.setFechaInicio(request.fechaInicio());
        renta.setFechaFin(request.fechaFin());

        return convertirResponse(rentaRepository.save(renta));
    }

    public RentaResponse actualizar(Integer id, RentaRequest request) {

        validarFechas(request);

        RentaEquipo renta = rentaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró la renta con ID: " + id));

        rentaRepository.findByCliente_IdCliente(request.idCliente())
                .filter(otraRenta -> !otraRenta.getIdRenta().equals(id))
                .ifPresent(otraRenta -> {
                    throw new IllegalArgumentException(
                            "El cliente ya tiene otra renta registrada");
                });

        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró el cliente"));

        Equipo equipo = equipoRepository.findById(request.idEquipo())
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró el equipo"));

        renta.setCliente(cliente);
        renta.setEquipo(equipo);
        renta.setRentaMensualEquipo(request.rentaMensualEquipo());
        renta.setFechaInicio(request.fechaInicio());
        renta.setFechaFin(request.fechaFin());

        return convertirResponse(rentaRepository.save(renta));
    }

    public void eliminar(Integer id) {
        RentaEquipo renta = rentaRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró la renta con ID: " + id));

        rentaRepository.delete(renta);
    }

    private void validarFechas(RentaRequest request) {

        if (request.fechaFin() != null
                && !request.fechaFin().isAfter(request.fechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private RentaResponse convertirResponse(RentaEquipo renta) {

        return new RentaResponse(
                renta.getIdRenta(),
                renta.getCliente().getIdCliente(),
                renta.getCliente().getUsuario() != null
                        ? (renta.getCliente().getUsuario().getNombre() + " "
                            + (renta.getCliente().getUsuario().getApellido() != null
                                ? renta.getCliente().getUsuario().getApellido() : "")).trim()
                        : renta.getCliente().getNombreContacto(),
                renta.getEquipo().getIdEquipo(),
                renta.getEquipo().getModelo(),
                renta.getEquipo().getMarca(),
                renta.getRentaMensualEquipo(),
                renta.getFechaInicio(),
                renta.getFechaFin()
        );
    }
}