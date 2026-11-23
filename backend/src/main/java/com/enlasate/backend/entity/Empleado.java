package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empleado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmpleado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    private String puesto;
}