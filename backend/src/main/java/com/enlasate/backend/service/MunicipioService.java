package com.enlasate.backend.service;

import com.enlasate.backend.dto.MunicipioResponse;
import com.enlasate.backend.entity.Municipio;
import com.enlasate.backend.repository.MunicipioRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MunicipioService {

    private final MunicipioRepository municipioRepository;

    public MunicipioService(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    public List<MunicipioResponse> listar() {
        return municipioRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public MunicipioResponse buscarPorId(Integer id) {
        Municipio municipio = municipioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Municipio no encontrado"));

        return convertirRespuesta(municipio);
    }

    public List<MunicipioResponse> buscarPorEstado(Integer idEstado) {
        return municipioRepository.findByEstado_IdEstado(idEstado)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
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