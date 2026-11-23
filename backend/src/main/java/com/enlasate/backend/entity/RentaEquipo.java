package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "renta_equipo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RentaEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRenta;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", unique = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo")
    private Equipo equipo;

    private BigDecimal rentaMensualEquipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}