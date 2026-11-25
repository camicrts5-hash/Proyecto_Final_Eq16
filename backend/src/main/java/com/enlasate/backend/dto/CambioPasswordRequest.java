package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CambioPasswordRequest(

        @NotBlank(message = "Escribe tu contraseña actual")
        String passwordActual,

        @NotBlank(message = "Escribe la contraseña nueva")
        @Size(min = 8, max = 60, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "La contraseña debe combinar letras y números"
        )
        String passwordNueva
) {}
