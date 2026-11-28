package com.enlasate.backend.service;

import com.enlasate.backend.dto.ColoniaRequest;
import com.enlasate.backend.dto.ColoniaResponse;
import com.enlasate.backend.entity.Colonia;
import com.enlasate.backend.entity.Municipio;
import com.enlasate.backend.repository.ColoniaRepository;
import com.enlasate.backend.repository.MunicipioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ColoniaService {

    private final ColoniaRepository coloniaRepository;
    private final MunicipioRepository municipioRepository;

    public ColoniaService(
            ColoniaRepository coloniaRepository,
            MunicipioRepository municipioRepository) {
        this.coloniaRepository = coloniaRepository;
        this.municipioRepository = municipioRepository;
    }

    @Transactional(readOnly = true)
    public List<ColoniaResponse> listarPorMunicipio(Integer idMunicipio) {

        return coloniaRepository
                .findByMunicipio_IdMunicipioOrderByNombreColoniaAsc(idMunicipio)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional
    public ColoniaResponse crear(ColoniaRequest request) {

        Integer idMunicipio = request.idMunicipio();

        if (idMunicipio == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Selecciona el municipio de la colonia");
        }

        String nombre = request.nombreColonia().trim();

        if (coloniaRepository
                .existsByMunicipio_IdMunicipioAndNombreColoniaIgnoreCase(idMunicipio, nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Esa colonia ya existe en ese municipio");
        }

        Municipio municipio = municipioRepository.findById(idMunicipio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Municipio no encontrado"));

        Colonia colonia = new Colonia();
        colonia.setMunicipio(municipio);
        colonia.setNombreColonia(nombre);

        return convertir(coloniaRepository.save(colonia));
    }

    @Transactional
    public ColoniaResponse actualizar(Integer id, ColoniaRequest request) {

        Colonia colonia = coloniaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Colonia no encontrada"));

        colonia.setNombreColonia(request.nombreColonia().trim());

        if (request.idMunicipio() != null) {
            Municipio municipio = municipioRepository.findById(request.idMunicipio())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Municipio no encontrado"));
            colonia.setMunicipio(municipio);
        }

        return convertir(coloniaRepository.save(colonia));
    }

    private ColoniaResponse convertir(Colonia colonia) {
        return new ColoniaResponse(
                colonia.getIdColonia(),
                colonia.getMunicipio() != null ? colonia.getMunicipio().getIdMunicipio() : null,
                colonia.getMunicipio() != null ? colonia.getMunicipio().getNombreMunicipio() : null,
                colonia.getNombreColonia()
        );
    }
}
