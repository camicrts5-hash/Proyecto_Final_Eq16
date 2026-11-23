package com.enlasate.backend.dto;

import java.time.LocalDateTime;

public record SoporteResponse(
        Integer idTicket,
        String cliente,
        String empleado,
        String descripcionFalla,
        LocalDateTime fechaReporte,
        String estadoTicket,
        LocalDateTime fechaCierre
) {}