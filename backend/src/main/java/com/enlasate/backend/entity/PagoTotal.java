package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pago_total")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan")
    private Plan plan;

    private BigDecimal monto;

    private String mesCorrespondiente;

    private LocalDate fechaPago;

    private String metodoPago;

    private String estadoPago;
}