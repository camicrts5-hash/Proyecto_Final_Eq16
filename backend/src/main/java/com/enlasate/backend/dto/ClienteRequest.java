package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;

public record ClienteRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene formato válido")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
        String telefono,

        @NotNull(message = "El plan es obligatorio")
        Integer idPlan,

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "Activo|Inactivo|Moroso", message = "Estado no válido")
        String estado
) {}