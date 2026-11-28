package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SoporteClienteRequest(
        @NotBlank(message = "Describe la falla que tienes")
        @Size(max = 255, message = "Máximo 255 caracteres")
        String descripcion
) {}
