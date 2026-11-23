package com.enlasate.backend.controller;

import com.enlasate.backend.dto.EquipoRequest;
import com.enlasate.backend.dto.EquipoResponse;
import com.enlasate.backend.service.EquipoService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<EquipoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(equipoService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<EquipoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(equipoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipoResponse> registrar(
            @Valid @RequestBody EquipoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipoService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EquipoRequest request) {
        return ResponseEntity.ok(equipoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}