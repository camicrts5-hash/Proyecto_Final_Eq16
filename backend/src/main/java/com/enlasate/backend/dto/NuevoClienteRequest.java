package com.enlasate.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NuevoClienteRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60)
        String nombre,

        @Size(max = 60)
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100)
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener 10 dígitos")
        String telefono,

        // Plan contratado (opcional: puede quedar sin plan al registrarse)
        Integer idPlan,

        // true  = se le crea cuenta para que entre a la app (con código)
        // false = solo queda el registro, sin cuenta (adultos mayores, etc.)
        Boolean conCuenta,

        // ------------------ Domicilio (obligatorio) ------------------
        @jakarta.validation.constraints.NotNull(message = "Selecciona el municipio")
        @jakarta.validation.constraints.Positive(message = "El municipio no es válido")
        Integer idMunicipio,

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 120)
        String calle,

        @NotBlank(message = "El número es obligatorio")
        @Size(max = 10)
        String numero,

        @NotBlank(message = "La colonia es obligatoria")
        @Size(max = 80)
        String colonia,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "\\d{5}", message = "El código postal debe tener 5 dígitos")
        String codigoPostal,

        Integer idColonia
) {}
