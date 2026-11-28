package com.enlasate.backend.controller;

import com.enlasate.backend.dto.MunicipioResponse;
import com.enlasate.backend.service.MunicipioService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/municipios")
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    // Catálogo público: cualquier usuario (o visitante) lo puede consultar
    @GetMapping
    public ResponseEntity<List<MunicipioResponse>> listar() {
        return ResponseEntity.ok(municipioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MunicipioResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(municipioService.buscarPorId(id));
    }

    @GetMapping("/estado/{idEstado}")
    public ResponseEntity<List<MunicipioResponse>> buscarPorEstado(
            @PathVariable Integer idEstado) {
        return ResponseEntity.ok(
                municipioService.buscarPorEstado(idEstado));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MunicipioResponse> crear(
            @jakarta.validation.Valid @RequestBody
            com.enlasate.backend.dto.MunicipioRequest request) {

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(municipioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MunicipioResponse> actualizar(
            @PathVariable Integer id,
            @jakarta.validation.Valid @RequestBody
            com.enlasate.backend.dto.MunicipioRequest request) {

        return ResponseEntity.ok(municipioService.actualizar(id, request));
    }
}
