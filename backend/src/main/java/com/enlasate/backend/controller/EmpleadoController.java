package com.enlasate.backend.controller;

import com.enlasate.backend.dto.EmpleadoRequest;
import com.enlasate.backend.dto.EmpleadoResponse;
import com.enlasate.backend.service.EmpleadoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    // -----------------------------------------------------------------
    // Lista de empleados (clientes eligen asesor, admin administra)
    // -----------------------------------------------------------------
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmpleadoResponse>> listar() {
        return ResponseEntity.ok(empleadoService.listar());
    }

    // -----------------------------------------------------------------
    // Alta de empleado (solo administrador)
    // -----------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpleadoResponse> crear(
            @Valid @RequestBody EmpleadoRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empleadoService.crear(request));
    }

    // -----------------------------------------------------------------
    // Editar empleado (solo administrador)
    // -----------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpleadoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EmpleadoRequest request) {

        return ResponseEntity.ok(empleadoService.actualizar(id, request));
    }

    // -----------------------------------------------------------------
    // Baja de empleado (solo administrador)
    // -----------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {

        empleadoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
