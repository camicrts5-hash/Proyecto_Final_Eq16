package com.enlasate.backend.controller;

import com.enlasate.backend.dto.PagoRequest;
import com.enlasate.backend.dto.PagoResponse;
import com.enlasate.backend.dto.PagoSimuladoRequest;
import com.enlasate.backend.service.PagoService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Registrar pago simulado del cliente autenticado
    @PostMapping("/mi-pago")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PagoResponse> registrarMiPago(
        Authentication authentication,
        @Valid @RequestBody PagoSimuladoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                pagoService.registrarPagoSimulado(
                    authentication.getName(),
                    request
                )
            );
    }

    // Consultar historial del cliente autenticado
    @GetMapping("/mi-historial")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Page<PagoResponse>> miHistorial(
        Authentication authentication,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            pagoService.listarPorEmail(
                authentication.getName(),
                pageable
            )
        );
    }

    // Confirmar un pago pendiente (transferencia, efectivo o depósito)
    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PagoResponse> confirmar(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.confirmarPago(id));
    }

    // -----------------------------------------------------------------
    // Estado de cuenta por año (ADMIN y EMPLEADO)
    // -----------------------------------------------------------------
    @GetMapping("/estado-cuenta/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<com.enlasate.backend.dto.EstadoCuentaResponse> estadoCuenta(
        @PathVariable Integer idCliente,
        @RequestParam(required = false) Integer anio
    ) {
        return ResponseEntity.ok(
            pagoService.estadoCuenta(idCliente, anio)
        );
    }

    // -----------------------------------------------------------------
    // Estado de cuenta del cliente autenticado
    // -----------------------------------------------------------------
    @GetMapping("/mi-estado-cuenta")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<com.enlasate.backend.dto.EstadoCuentaResponse> miEstadoCuenta(
        Authentication authentication,
        @RequestParam(required = false) Integer anio
    ) {
        Integer idCliente = pagoService.idClientePorEmail(authentication.getName());

        return ResponseEntity.ok(
            pagoService.estadoCuenta(idCliente, anio)
        );
    }

    // Listar todos los pagos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<PagoResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(pagoService.listar(pageable));
    }

    // Historial de un cliente por ID
    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Page<PagoResponse>> listarPorCliente(
        @PathVariable Integer idCliente,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            pagoService.listarPorCliente(idCliente, pageable)
        );
    }

    // Buscar pago por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PagoResponse> buscarPorId(
        @PathVariable Integer id
    ) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    // Registrar pago manualmente (efectivo o depósito: empleado o admin)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PagoResponse> registrar(
        @Valid @RequestBody PagoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(pagoService.registrar(request));
    }

    // Actualizar pago
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoResponse> actualizar(
        @PathVariable Integer id,
        @Valid @RequestBody PagoRequest request
    ) {
        return ResponseEntity.ok(
            pagoService.actualizar(id, request)
        );
    }

    // Eliminar pago
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
        @PathVariable Integer id
    ) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}