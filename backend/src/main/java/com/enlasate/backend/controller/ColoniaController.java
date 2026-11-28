package com.enlasate.backend.controller;

import com.enlasate.backend.dto.ColoniaRequest;
import com.enlasate.backend.dto.ColoniaResponse;
import com.enlasate.backend.service.ColoniaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colonias")
public class ColoniaController {

    private final ColoniaService coloniaService;

    public ColoniaController(ColoniaService coloniaService) {
        this.coloniaService = coloniaService;
    }

    @GetMapping
    public ResponseEntity<List<ColoniaResponse>> listar(
            @RequestParam Integer idMunicipio) {

        return ResponseEntity.ok(coloniaService.listarPorMunicipio(idMunicipio));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColoniaResponse> crear(
            @Valid @RequestBody ColoniaRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coloniaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColoniaResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ColoniaRequest request) {

        return ResponseEntity.ok(coloniaService.actualizar(id, request));
    }
}
