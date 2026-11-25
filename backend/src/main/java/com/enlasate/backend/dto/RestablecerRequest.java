package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(

        @NotBlank(message = "Falta el token de recuperación")
        String token,

        @NotBlank(message = "Escribe la contraseña nueva")
        @Size(min = 8, max = 60, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "La contraseña debe combinar letras y números"
        )
        String passwordNueva
) {}
