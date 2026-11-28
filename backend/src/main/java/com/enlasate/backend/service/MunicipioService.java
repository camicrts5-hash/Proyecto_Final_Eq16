package com.enlasate.backend.service;

import com.enlasate.backend.dto.MunicipioResponse;
import com.enlasate.backend.entity.Municipio;
import com.enlasate.backend.repository.MunicipioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MunicipioService {

    private final MunicipioRepository municipioRepository;
    private final com.enlasate.backend.repository.EstadoRepository estadoRepository;

    public MunicipioService(
            MunicipioRepository municipioRepository,
            com.enlasate.backend.repository.EstadoRepository estadoRepository) {
        this.municipioRepository = municipioRepository;
        this.estadoRepository = estadoRepository;
    }

    @Transactional(readOnly = true)
    public List<MunicipioResponse> listar() {
        return municipioRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public MunicipioResponse buscarPorId(Integer id) {
        Municipio municipio = municipioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Municipio no encontrado"));

        return convertirRespuesta(municipio);
    }

    @Transactional(readOnly = true)
    public List<MunicipioResponse> buscarPorEstado(Integer idEstado) {
        return municipioRepository.findByEstado_IdEstado(idEstado)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    // -----------------------------------------------------------------
    // Agregar un municipio nuevo al catálogo
    // -----------------------------------------------------------------
    @Transactional
    public MunicipioResponse crear(com.enlasate.backend.dto.MunicipioRequest request) {

        Integer idEstado = request.idEstado() != null ? request.idEstado() : 1;
        String nombre = request.nombreMunicipio().trim();

        // ¿Ya existe ese municipio en ese estado?
        boolean existe = municipioRepository.findByEstado_IdEstado(idEstado)
                .stream()
                .anyMatch(m -> m.getNombreMunicipio().equalsIgnoreCase(nombre));

        if (existe) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Ese municipio ya está registrado"
            );
        }

        Municipio municipio = new Municipio();
        municipio.setNombreMunicipio(nombre);

        com.enlasate.backend.entity.Estado estado = estadoRepository.findById(idEstado)
                .orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));

        municipio.setEstado(estado);

        return convertirRespuesta(municipioRepository.save(municipio));
    }

    // -----------------------------------------------------------------
    // Estados (para poder agregarlos y editarlos como los municipios)
    // -----------------------------------------------------------------
    @Transactional(readOnly = true)
    public java.util.List<com.enlasate.backend.dto.EstadoResponse> listarEstados() {
        return estadoRepository.findAll()
                .stream()
                .map(e -> new com.enlasate.backend.dto.EstadoResponse(
                        e.getIdEstado(), e.getNombreEstado()))
                .toList();
    }

    @Transactional
    public com.enlasate.backend.dto.EstadoResponse crearEstado(String nombre) {

        String limpio = nombre.trim();

        boolean existe = estadoRepository.findAll()
                .stream()
                .anyMatch(e -> e.getNombreEstado().equalsIgnoreCase(limpio));

        if (existe) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Ese estado ya está registrado");
        }

        com.enlasate.backend.entity.Estado estado = new com.enlasate.backend.entity.Estado();
        estado.setNombreEstado(limpio);

        estado = estadoRepository.save(estado);

        return new com.enlasate.backend.dto.EstadoResponse(
                estado.getIdEstado(), estado.getNombreEstado());
    }

    @Transactional
    public com.enlasate.backend.dto.EstadoResponse actualizarEstado(Integer id, String nombre) {

        com.enlasate.backend.entity.Estado estado = estadoRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Estado no encontrado"));

        estado.setNombreEstado(nombre.trim());
        estado = estadoRepository.save(estado);

        return new com.enlasate.backend.dto.EstadoResponse(
                estado.getIdEstado(), estado.getNombreEstado());
    }
    @Transactional
    public MunicipioResponse actualizar(Integer id, com.enlasate.backend.dto.MunicipioRequest request) {

        Municipio municipio = municipioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Municipio no encontrado"));

        String nombre = request.nombreMunicipio().trim();

        municipio.setNombreMunicipio(nombre);

        if (request.idEstado() != null) {
            com.enlasate.backend.entity.Estado estado = estadoRepository.findById(request.idEstado())
                    .orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));
            municipio.setEstado(estado);
        }

        return convertirRespuesta(municipioRepository.save(municipio));
    }

    private MunicipioResponse convertirRespuesta(Municipio municipio) {
        return new MunicipioResponse(
                municipio.getIdMunicipio(),
                municipio.getEstado().getIdEstado(),
                municipio.getEstado().getNombreEstado(),
                municipio.getNombreMunicipio()
        );
    }
}