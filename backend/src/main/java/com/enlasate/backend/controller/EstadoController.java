package com.enlasate.backend.controller;

import com.enlasate.backend.dto.EstadoResponse;
import com.enlasate.backend.service.EstadoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<List<EstadoResponse>> listar() {
        return ResponseEntity.ok(estadoService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<EstadoResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(estadoService.buscarPorId(id));
    }
}