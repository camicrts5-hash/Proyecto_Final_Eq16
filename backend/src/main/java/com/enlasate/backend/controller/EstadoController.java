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
    private final com.enlasate.backend.service.MunicipioService municipioService;

    public EstadoController(
            EstadoService estadoService,
            com.enlasate.backend.service.MunicipioService municipioService) {
        this.estadoService = estadoService;
        this.municipioService = municipioService;
    }

    @GetMapping
    public ResponseEntity<List<EstadoResponse>> listar() {
        return ResponseEntity.ok(estadoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadoResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(estadoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadoResponse> crear(
            @RequestBody java.util.Map<String, String> body) {

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(municipioService.crearEstado(body.get("nombreEstado")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstadoResponse> actualizar(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> body) {

        return ResponseEntity.ok(
                municipioService.actualizarEstado(id, body.get("nombreEstado")));
    }
}