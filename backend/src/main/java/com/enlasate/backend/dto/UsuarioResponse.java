package com.enlasate.backend.dto;

public record UsuarioResponse(
        Integer idUsuario,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String estado,
        String rol,
        Integer idCliente
) {}