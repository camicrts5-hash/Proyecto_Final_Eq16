package com.enlasate.backend.dto;

public record AuthResponse(
        String token,
        String tipo,
        Integer idUsuario,
        String nombre,
        String email,
        String rol
) {}