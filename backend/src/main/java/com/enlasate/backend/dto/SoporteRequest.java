package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record SoporteRequest(

    @NotNull(message = "El cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser válido")
    Integer idCliente,

    @NotNull(message = "El empleado es obligatorio")
    @Positive(message = "El ID del empleado debe ser válido")
    Integer idEmpleado,

    @NotBlank(message = "La descripción de la falla es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String descripcionFalla,

    @NotNull(message = "La fecha de reporte es obligatoria")
    LocalDateTime fechaReporte,

    @NotBlank(message = "El estado del ticket es obligatorio")
    @Pattern(
        regexp = "Abierto|En proceso|Cerrado",
        message = "El estado debe ser Abierto, En proceso o Cerrado"
    )
    String estadoTicket,

    LocalDateTime fechaCierre

) {}