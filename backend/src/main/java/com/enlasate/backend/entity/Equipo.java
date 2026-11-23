package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "equipo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEquipo;

    private BigDecimal costoEmp;
    private String modelo;
    private String marca;
    private String estadoEquipo;
}