package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}