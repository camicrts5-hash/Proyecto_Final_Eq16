package com.enlasate.backend.controller;

import com.enlasate.backend.dto.CompraRequest;
import com.enlasate.backend.dto.CompraResponse;
import com.enlasate.backend.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<CompraResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(compraService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<CompraResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(compraService.buscarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<CompraResponse> buscarPorCliente(
            @PathVariable Integer idCliente) {
        return ResponseEntity.ok(compraService.buscarPorCliente(idCliente));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompraResponse> registrar(
            @Valid @RequestBody CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(compraService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompraResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CompraRequest request) {
        return ResponseEntity.ok(compraService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        compraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}