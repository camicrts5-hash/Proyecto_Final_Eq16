package com.enlasate.backend.dto;

import java.time.LocalDateTime;

public record SoporteResponse(
    Integer idTicket,
    Integer idCliente,
    String nombreCliente,
    Integer idEmpleado,
    String nombreEmpleado,
    String descripcionFalla,
    LocalDateTime fechaReporte,
    String estadoTicket,
    LocalDateTime fechaCierre
) {}