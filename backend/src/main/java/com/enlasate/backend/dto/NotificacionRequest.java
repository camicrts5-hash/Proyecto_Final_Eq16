package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record NotificacionRequest(

        @NotNull(message = "El usuario es obligatorio")
        @Positive(message = "El ID del usuario debe ser positivo")
        Integer idUsuario,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 255, message = "El mensaje no puede superar 255 caracteres")
        String mensaje,

        LocalDateTime fechaEnvio,

        @NotBlank(message = "El tipo de notificación es obligatorio")
        @Pattern(
                regexp = "Correo|WhatsApp|SMS",
                message = "El tipo debe ser Correo, WhatsApp o SMS"
        )
        String tipo
) {}