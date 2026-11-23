package com.enlasate.backend.controller;

import com.enlasate.backend.dto.ClienteRequest;
import com.enlasate.backend.dto.ClienteResponse;
import com.enlasate.backend.service.ClienteService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Listar clientes
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<ClienteResponse>> listar(
            Pageable pageable) {

        return ResponseEntity.ok(
                clienteService.listar(pageable)
        );
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                clienteService.buscarPorId(id)
        );
    }

    // Actualizar cliente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request) {

        return ResponseEntity.ok(
                clienteService.actualizar(id, request)
        );
    }
}