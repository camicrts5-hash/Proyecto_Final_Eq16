package com.enlasate.backend.service;

import com.enlasate.backend.dto.EquipoRequest;
import com.enlasate.backend.dto.EquipoResponse;
import com.enlasate.backend.entity.Equipo;
import com.enlasate.backend.repository.EquipoRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public Page<EquipoResponse> listar(Pageable pageable) {
        return equipoRepository.findAll(pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public EquipoResponse buscarPorId(Integer id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el equipo con ID: " + id
                ));

        return convertirRespuesta(equipo);
    }

    @Transactional
    public EquipoResponse registrar(EquipoRequest request) {
        Equipo equipo = new Equipo();

        equipo.setCostoEmp(request.costoEmp());
        equipo.setModelo(request.modelo());
        equipo.setMarca(request.marca());
        equipo.setEstadoEquipo(request.estadoEquipo());

        equipo = equipoRepository.save(equipo);

        return convertirRespuesta(equipo);
    }

    @Transactional
    public EquipoResponse actualizar(Integer id, EquipoRequest request) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el equipo con ID: " + id
                ));

        equipo.setCostoEmp(request.costoEmp());
        equipo.setModelo(request.modelo());
        equipo.setMarca(request.marca());
        equipo.setEstadoEquipo(request.estadoEquipo());

        equipo = equipoRepository.save(equipo);

        return convertirRespuesta(equipo);
    }

    @Transactional
    public void eliminar(Integer id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el equipo con ID: " + id
                ));

        equipoRepository.delete(equipo);
    }

    private EquipoResponse convertirRespuesta(Equipo equipo) {
        return new EquipoResponse(
                equipo.getIdEquipo(),
                equipo.getCostoEmp(),
                equipo.getModelo(),
                equipo.getMarca(),
                equipo.getEstadoEquipo()
        );
    }
}