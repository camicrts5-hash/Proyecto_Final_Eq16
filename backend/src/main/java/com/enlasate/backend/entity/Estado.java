package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEstado;

    private String nombreEstado;
}