package com.enlasate.backend.controller;

import com.enlasate.backend.dto.DomicilioRequest;
import com.enlasate.backend.dto.DomicilioResponse;
import com.enlasate.backend.service.DomicilioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domicilios")
public class DomicilioController {

    private final DomicilioService domicilioService;

    public DomicilioController(DomicilioService domicilioService) {
        this.domicilioService = domicilioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<DomicilioResponse>> listar() {
        return ResponseEntity.ok(domicilioService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<DomicilioResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(domicilioService.buscarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<DomicilioResponse>> buscarPorCliente(
            @PathVariable Integer idCliente) {
        return ResponseEntity.ok(
                domicilioService.buscarPorCliente(idCliente));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<DomicilioResponse> registrar(
            @Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(domicilioService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<DomicilioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.ok(
                domicilioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        domicilioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}