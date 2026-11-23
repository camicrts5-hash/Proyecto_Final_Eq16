package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plan")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlan;

    private String nombrePlan;
    private Integer velocidadMb;
    private BigDecimal precioMensual;
}