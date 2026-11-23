package com.enlasate.backend.controller;

import com.enlasate.backend.dto.RentaRequest;
import com.enlasate.backend.dto.RentaResponse;
import com.enlasate.backend.service.RentaService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentas")
public class RentaController {

    private final RentaService rentaService;

    public RentaController(RentaService rentaService) {
        this.rentaService = rentaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<RentaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(rentaService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<RentaResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(rentaService.buscarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<RentaResponse> buscarPorCliente(
            @PathVariable Integer idCliente) {
        return ResponseEntity.ok(rentaService.buscarPorCliente(idCliente));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentaResponse> registrar(
            @Valid @RequestBody RentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentaService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentaResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody RentaRequest request) {
        return ResponseEntity.ok(rentaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        rentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}