package com.enlasate.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60)
        String nombre,

        @Size(max = 60)
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120)
        String email
) {}
