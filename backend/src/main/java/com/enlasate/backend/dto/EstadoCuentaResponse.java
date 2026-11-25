package com.enlasate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EstadoCuentaResponse(
        Integer idCliente,
        String nombre,
        String email,
        String telefono,
        String plan,
        Integer idPlan,
        BigDecimal precioPlan,
        String estado,
        String empleado,
        LocalDate fechaAlta,
        LocalDateTime fechaInstalacion,
        Integer anio,
        List<Integer> aniosDisponibles,
        List<MesEstado> meses,
        BigDecimal totalPagado,
        Integer mesesPagados,
        boolean servicioActivo,
        LocalDate inicioServicio
) {

    public record MesEstado(
            int numero,
            String nombre,
            boolean pagado,
            boolean aplica,
            boolean enRevision,
            Integer idPago,
            BigDecimal monto,
            LocalDate fechaPago,
            String metodoPago
    ) {}
}
