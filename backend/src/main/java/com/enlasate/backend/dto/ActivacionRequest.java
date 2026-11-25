package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivacionRequest(
        @NotBlank(message = "El código de activación es obligatorio")
        String codigo
) {}
