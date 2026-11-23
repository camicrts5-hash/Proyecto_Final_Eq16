package com.enlasate.backend.dto;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Integer idNotificacion,
        Integer idUsuario,
        String nombreUsuario,
        String emailUsuario,
        String mensaje,
        LocalDateTime fechaEnvio,
        String tipo
) {}