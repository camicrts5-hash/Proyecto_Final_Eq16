package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MunicipioRequest(
        @NotBlank(message = "El nombre del municipio es obligatorio")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres")
        String nombreMunicipio,

        Integer idEstado
) {}
