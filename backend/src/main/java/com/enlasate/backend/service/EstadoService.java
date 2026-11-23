package com.enlasate.backend.service;

import com.enlasate.backend.dto.EstadoResponse;
import com.enlasate.backend.entity.Estado;
import com.enlasate.backend.repository.EstadoRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadoService {

    private final EstadoRepository estadoRepository;

    public EstadoService(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    public List<EstadoResponse> listar() {
        return estadoRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    public EstadoResponse buscarPorId(Integer id) {
        Estado estado = estadoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Estado no encontrado"));

        return convertirRespuesta(estado);
    }

    private EstadoResponse convertirRespuesta(Estado estado) {
        return new EstadoResponse(
                estado.getIdEstado(),
                estado.getNombreEstado()
        );
    }
}