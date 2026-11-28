package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ColoniaRequest(
        @NotBlank(message = "El nombre de la colonia es obligatorio")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres")
        String nombreColonia,

        Integer idMunicipio
) {}
