package com.enlasate.backend.dto;

public record EmpleadoResponse(
        Integer idEmpleado,
        String nombre,
        String email,
        String telefono,
        String puesto,
        String rol
) {}
