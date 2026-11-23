package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "soporte_tecnico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SoporteTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    private String descripcionFalla;
    private LocalDateTime fechaReporte;
    private String estadoTicket;
    private LocalDateTime fechaCierre;
}