package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "periodo_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPeriodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    private LocalDate fechaInicio;

    // NULL = el servicio sigue activo
    private LocalDate fechaFin;

    private String motivo;
}
