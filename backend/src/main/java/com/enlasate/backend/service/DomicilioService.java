package com.enlasate.backend.service;

import com.enlasate.backend.dto.DomicilioRequest;
import com.enlasate.backend.dto.DomicilioResponse;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.Domicilio;
import com.enlasate.backend.entity.Municipio;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.DomicilioRepository;
import com.enlasate.backend.repository.MunicipioRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DomicilioService {

    private final DomicilioRepository domicilioRepository;
    private final ClienteRepository clienteRepository;
    private final MunicipioRepository municipioRepository;

    public DomicilioService(
            DomicilioRepository domicilioRepository,
            ClienteRepository clienteRepository,
            MunicipioRepository municipioRepository) {
        this.domicilioRepository = domicilioRepository;
        this.clienteRepository = clienteRepository;
        this.municipioRepository = municipioRepository;
    }

    @Transactional(readOnly = true)
    public List<DomicilioResponse> listar() {
        return domicilioRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DomicilioResponse buscarPorId(Integer id) {

        Domicilio domicilio = domicilioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el domicilio con ID: " + id));

        return convertirResponse(domicilio);
    }

    @Transactional(readOnly = true)
    public List<DomicilioResponse> buscarPorCliente(Integer idCliente) {

        if (!clienteRepository.existsById(idCliente)) {
            throw new EntityNotFoundException(
                    "No se encontró el cliente con ID: " + idCliente);
        }

        return domicilioRepository.findByCliente_IdCliente(idCliente)
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    public DomicilioResponse registrar(DomicilioRequest request) {

        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el cliente"));

        Municipio municipio = municipioRepository.findById(request.idMunicipio())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el municipio"));

        Domicilio domicilio = new Domicilio();

        domicilio.setCliente(cliente);
        domicilio.setMunicipio(municipio);
        domicilio.setCalle(request.calle());
        domicilio.setNumero(request.numero());
        domicilio.setColonia(request.colonia());
        domicilio.setCodigoPostal(request.codigoPostal());
        domicilio.setIdColonia(request.idColonia());

        return convertirResponse(domicilioRepository.save(domicilio));
    }

    public DomicilioResponse actualizar(
            Integer id, DomicilioRequest request) {

        Domicilio domicilio = domicilioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el domicilio con ID: " + id));

        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el cliente"));

        Municipio municipio = municipioRepository.findById(request.idMunicipio())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el municipio"));

        domicilio.setCliente(cliente);
        domicilio.setMunicipio(municipio);
        domicilio.setCalle(request.calle());
        domicilio.setNumero(request.numero());
        domicilio.setColonia(request.colonia());
        domicilio.setCodigoPostal(request.codigoPostal());
        domicilio.setIdColonia(request.idColonia());

        return convertirResponse(domicilioRepository.save(domicilio));
    }

    public void eliminar(Integer id) {

        Domicilio domicilio = domicilioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el domicilio con ID: " + id));

        domicilioRepository.delete(domicilio);
    }

    private DomicilioResponse convertirResponse(Domicilio domicilio) {

        // Un cliente sin cuenta en la web no tiene usuario: se usa el contacto
        String nombreCliente;

        if (domicilio.getCliente() == null) {
            nombreCliente = null;
        } else if (domicilio.getCliente().getUsuario() != null) {
            nombreCliente = (domicilio.getCliente().getUsuario().getNombre() + " "
                    + (domicilio.getCliente().getUsuario().getApellido() != null
                        ? domicilio.getCliente().getUsuario().getApellido() : "")).trim();
        } else {
            nombreCliente = domicilio.getCliente().getNombreContacto();
        }

        return new DomicilioResponse(
                domicilio.getIdDomicilio(),
                domicilio.getCliente() != null ? domicilio.getCliente().getIdCliente() : null,
                nombreCliente,
                domicilio.getMunicipio() != null ? domicilio.getMunicipio().getIdMunicipio() : null,
                domicilio.getMunicipio() != null ? domicilio.getMunicipio().getNombreMunicipio() : null,
                domicilio.getMunicipio() != null && domicilio.getMunicipio().getEstado() != null
                        ? domicilio.getMunicipio().getEstado().getNombreEstado() : null,
                domicilio.getCalle(),
                domicilio.getNumero(),
                domicilio.getColonia(),
                domicilio.getCodigoPostal(),
                domicilio.getIdColonia()
        );
    }
}