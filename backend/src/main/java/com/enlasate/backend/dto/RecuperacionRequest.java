package com.enlasate.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperacionRequest(
        @NotBlank(message = "Escribe tu correo")
        @Email(message = "El correo no tiene un formato válido")
        String email
) {}
