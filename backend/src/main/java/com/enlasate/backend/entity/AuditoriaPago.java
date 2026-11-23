package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_pago")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AuditoriaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAuditoria;

    private Integer idPago;
    private String accion;
    private BigDecimal montoAnterior;
    private BigDecimal montoNuevo;
    private String usuarioBd;
    private LocalDateTime fechaEvento;
}