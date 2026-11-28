package com.enlasate.backend.controller;

import com.enlasate.backend.dto.ActivacionRequest;
import com.enlasate.backend.dto.ClienteRequest;
import com.enlasate.backend.dto.ClienteResponse;
import com.enlasate.backend.dto.EmpleadoSeleccionRequest;
import com.enlasate.backend.dto.NuevoClienteRequest;
import com.enlasate.backend.dto.PlanSeleccionRequest;
import com.enlasate.backend.service.ClienteService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean conCuenta,
            @RequestParam(required = false) Integer idEstado,
            @RequestParam(required = false) Integer idMunicipio,
            @RequestParam(required = false) String colonia,
            @RequestParam(required = false) Boolean pagoMes,
            Pageable pageable) {

        return ResponseEntity.ok(
                clienteService.listar(q, estado, conCuenta, idEstado, idMunicipio, colonia, pagoMes, pageable)
        );
    }

    // -----------------------------------------------------------------
    // Domicilio del cliente autenticado
    // -----------------------------------------------------------------
    @GetMapping("/mi-domicilio")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<java.util.List<com.enlasate.backend.dto.DomicilioResponse>> miDomicilio(
            Authentication authentication) {

        return ResponseEntity.ok(clienteService.miDomicilio(authentication.getName()));
    }

    @PutMapping("/mi-domicilio")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<Void> guardarMiDomicilio(
            @RequestBody com.enlasate.backend.dto.DomicilioRequest request,
            Authentication authentication) {

        clienteService.guardarMiDomicilio(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------
    // Clientes agrupados por municipio y colonia
    // -----------------------------------------------------------------
    @GetMapping("/por-zona")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<java.util.List<com.enlasate.backend.dto.ZonaResponse>> porZona() {

        return ResponseEntity.ok(clienteService.porZona());
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

    // Seleccionar plan del cliente (solo personal: el cliente no puede)
    @PutMapping("/mi-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> seleccionarPlan(
            @Valid @RequestBody PlanSeleccionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                clienteService.seleccionarPlan(
                        authentication.getName(),
                        request.idPlan()
                )
        );
    }
    // Consultar los datos del cliente autenticado
@GetMapping("/mi-plan")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
public ResponseEntity<ClienteResponse> consultarMiPlan(
        Authentication authentication) {

    return ResponseEntity.ok(
            clienteService.consultarMiPlan(authentication.getName())
    );
}

    // Actualizar cliente (admin y empleado)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request) {

        return ResponseEntity.ok(
                clienteService.actualizar(id, request)
        );
    }

// Eliminar cliente
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {

        clienteService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------
    // El cliente elige al empleado que lo atenderá
    // -----------------------------------------------------------------
    @PutMapping("/mi-empleado")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<ClienteResponse> asignarEmpleado(
            @Valid @RequestBody EmpleadoSeleccionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                clienteService.asignarEmpleado(
                        authentication.getName(),
                        request.idEmpleado()
                )
        );
    }

    // -----------------------------------------------------------------
    // Alta manual de cliente (empleado o admin) + código de activación
    // -----------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> crearManual(
            @Valid @RequestBody NuevoClienteRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                clienteService.crearManual(request, authentication.getName())
        );
    }

    // -----------------------------------------------------------------
    // Suspender el servicio (admin y empleado)
    // -----------------------------------------------------------------
    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> suspender(
            @PathVariable Integer id,
            @RequestBody(required = false) java.util.Map<String, String> cuerpo) {

        String motivo = cuerpo != null ? cuerpo.get("motivo") : null;

        return ResponseEntity.ok(clienteService.suspender(id, motivo));
    }

    // -----------------------------------------------------------------
    // Reactivar el servicio (admin y empleado)
    // -----------------------------------------------------------------
    @PostMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> reactivar(@PathVariable Integer id) {

        return ResponseEntity.ok(clienteService.reactivar(id));
    }

    // -----------------------------------------------------------------
    // Restablecer la contraseña de un cliente (admin y empleado)
    // -----------------------------------------------------------------
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<Void> resetPassword(@PathVariable Integer id) {

        clienteService.resetPassword(id);

        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------
    // Crear cuenta web para un cliente que se registró sin cuenta
    // -----------------------------------------------------------------
    @PostMapping("/{id}/crear-cuenta")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> crearCuenta(@PathVariable Integer id) {

        return ResponseEntity.ok(clienteService.crearCuenta(id));
    }

    // -----------------------------------------------------------------
    // Volver a generar el código de un cliente pendiente
    // -----------------------------------------------------------------
    @PostMapping("/{id}/codigo")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> regenerarCodigo(
            @PathVariable Integer id) {

        return ResponseEntity.ok(clienteService.regenerarCodigo(id));
    }

    // -----------------------------------------------------------------
    // El cliente activa su propia cuenta con el código de su asesor
    // -----------------------------------------------------------------
    @PostMapping("/mi-activacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<ClienteResponse> activarMiCuenta(
            @Valid @RequestBody ActivacionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                clienteService.activarPorCodigo(
                        authentication.getName(),
                        request.codigo()
                )
        );
    }

    // -----------------------------------------------------------------
    // El empleado o el admin activan el servicio tras instalar el equipo
    // -----------------------------------------------------------------
    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<ClienteResponse> activar(
            @PathVariable Integer id,
            @Valid @RequestBody ActivacionRequest request) {

        return ResponseEntity.ok(
                clienteService.activar(id, request.codigo())
        );
    }

}