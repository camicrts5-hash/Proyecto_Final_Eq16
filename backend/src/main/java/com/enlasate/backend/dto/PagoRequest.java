package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoRequest(

    @NotNull(message = "El cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser válido")
    Integer idCliente,

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    @NotBlank(message = "El mes correspondiente es obligatorio")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "El mes debe tener formato YYYY-MM")
    String mesCorrespondiente,

    @NotNull(message = "La fecha de pago es obligatoria")
    LocalDate fechaPago,

    @NotBlank(message = "El método de pago es obligatorio")
    String metodoPago

) {}