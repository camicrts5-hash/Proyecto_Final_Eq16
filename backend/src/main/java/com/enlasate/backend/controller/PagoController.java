package com.enlasate.backend.controller;

import com.enlasate.backend.dto.PagoRequest;
import com.enlasate.backend.dto.PagoResponse;
import com.enlasate.backend.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<PagoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(pagoService.listar(pageable));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<PagoResponse>> listarPorCliente(
            @PathVariable Integer idCliente, Pageable pageable) {
        return ResponseEntity.ok(
            pagoService.listarPorCliente(idCliente, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PagoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoResponse> registrar(
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(pagoService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}