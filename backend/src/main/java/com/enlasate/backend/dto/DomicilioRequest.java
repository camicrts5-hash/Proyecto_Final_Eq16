package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;

public record DomicilioRequest(

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El ID del cliente debe ser positivo")
        Integer idCliente,

        @NotNull(message = "El municipio es obligatorio")
        @Positive(message = "El ID del municipio debe ser positivo")
        Integer idMunicipio,

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 120, message = "La calle no puede superar 120 caracteres")
        String calle,

        @NotBlank(message = "El número es obligatorio")
        @Size(max = 10, message = "El número no puede superar 10 caracteres")
        String numero,

        @NotBlank(message = "La colonia es obligatoria")
        @Size(max = 80, message = "La colonia no puede superar 80 caracteres")
        String colonia,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "\\d{5}", message = "El código postal debe tener exactamente 5 dígitos")
        String codigoPostal,

        Integer idColonia
) {}