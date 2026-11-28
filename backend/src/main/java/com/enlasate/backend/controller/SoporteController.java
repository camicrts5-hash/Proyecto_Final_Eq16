package com.enlasate.backend.controller;

import com.enlasate.backend.dto.SoporteRequest;
import com.enlasate.backend.dto.SoporteResponse;
import com.enlasate.backend.service.SoporteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/soporte")
public class SoporteController {

    private final SoporteService soporteService;

    public SoporteController(SoporteService soporteService) {
        this.soporteService = soporteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<SoporteResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(soporteService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<SoporteResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(soporteService.buscarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<SoporteResponse>> listarPorCliente(
            @PathVariable Integer idCliente, Pageable pageable) {
        return ResponseEntity.ok(
            soporteService.listarPorCliente(idCliente, pageable));
    }

    @GetMapping("/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<SoporteResponse>> listarPorEmpleado(
            @PathVariable Integer idEmpleado, Pageable pageable) {
        return ResponseEntity.ok(
            soporteService.listarPorEmpleado(idEmpleado, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<SoporteResponse> registrar(
            @Valid @RequestBody SoporteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(soporteService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<SoporteResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody SoporteRequest request) {
        return ResponseEntity.ok(
            soporteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        soporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------
    // El cliente autenticado reporta una falla
    // -----------------------------------------------------------------
    @PostMapping("/mi-ticket")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SoporteResponse> reportarFalla(
            @Valid @RequestBody com.enlasate.backend.dto.SoporteClienteRequest request,
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
            soporteService.registrarPorCliente(
                authentication.getName(),
                request.descripcion()
            )
        );
    }

    // -----------------------------------------------------------------
    // Tickets del cliente autenticado
    // -----------------------------------------------------------------
    @GetMapping("/mis-tickets")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Page<SoporteResponse>> misTickets(
            Pageable pageable,
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
            soporteService.listarPorEmail(authentication.getName(), pageable)
        );
    }
}