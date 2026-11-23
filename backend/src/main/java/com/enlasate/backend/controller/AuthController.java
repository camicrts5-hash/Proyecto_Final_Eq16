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
}