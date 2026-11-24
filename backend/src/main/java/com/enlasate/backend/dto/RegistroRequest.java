package com.enlasate.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 40, message = "Máximo 40 caracteres")
    String usuario,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    String nombre,

    @Size(max = 60, message = "Máximo 60 caracteres")
    String apellido,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene formato válido")
    String email,

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    String telefono,

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
    )
    String password,

    Integer idPlan

) {}