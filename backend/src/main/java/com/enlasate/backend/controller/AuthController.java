package com.enlasate.backend.controller;

import java.util.Map;

import com.enlasate.backend.dto.AuthResponse;
import com.enlasate.backend.dto.LoginRequest;
import com.enlasate.backend.dto.RegistroRequest;
import com.enlasate.backend.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(
            @Valid @RequestBody RegistroRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registro(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {

        return ResponseEntity.ok(
                Map.of("mensaje", "Sesión cerrada correctamente")
        );
    }

    // -----------------------------------------------------------------
    // Cambio de contraseña del usuario autenticado
    // -----------------------------------------------------------------
    @PutMapping("/password")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @Valid @RequestBody com.enlasate.backend.dto.CambioPasswordRequest request,
            org.springframework.security.core.Authentication authentication) {

        authService.cambiarPassword(authentication.getName(), request);

        return ResponseEntity.ok(
                Map.of("mensaje", "Contraseña actualizada correctamente")
        );
    }

    // -----------------------------------------------------------------
    // Actualizar el perfil propio (solo administrador)
    // -----------------------------------------------------------------
    @PutMapping("/perfil")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> actualizarPerfil(
            @Valid @RequestBody com.enlasate.backend.dto.PerfilRequest request,
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                authService.actualizarPerfil(authentication.getName(), request)
        );
    }

    // -----------------------------------------------------------------
    // Recuperación de contraseña (público)
    // -----------------------------------------------------------------
    @PostMapping("/recuperar")
    public ResponseEntity<Map<String, String>> recuperar(
            @Valid @RequestBody com.enlasate.backend.dto.RecuperacionRequest request) {

        authService.solicitarRecuperacion(request.email());

        return ResponseEntity.ok(Map.of(
                "mensaje",
                "Si el correo existe, te enviamos un enlace para restablecer tu contraseña"
        ));
    }

    // -----------------------------------------------------------------
    // Restablecer con el token del correo (público)
    // -----------------------------------------------------------------
    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(
            @Valid @RequestBody com.enlasate.backend.dto.RestablecerRequest request) {

        authService.restablecerPassword(request.token(), request.passwordNueva());

        return ResponseEntity.ok(Map.of(
                "mensaje",
                "Contraseña actualizada. Ya puedes iniciar sesión"
        ));
    }
}