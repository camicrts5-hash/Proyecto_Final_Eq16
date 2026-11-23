package com.enlasate.backend.controller;

import com.enlasate.backend.dto.NotificacionRequest;
import com.enlasate.backend.dto.NotificacionResponse;
import com.enlasate.backend.service.NotificacionService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(
            NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<NotificacionResponse>> listar(
            Pageable pageable) {
        return ResponseEntity.ok(
                notificacionService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<NotificacionResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                notificacionService.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<NotificacionResponse>> buscarPorUsuario(
            @PathVariable Integer idUsuario,
            Pageable pageable) {
        return ResponseEntity.ok(
                notificacionService.buscarPorUsuario(idUsuario, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<NotificacionResponse> registrar(
            @Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<NotificacionResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.ok(
                notificacionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}