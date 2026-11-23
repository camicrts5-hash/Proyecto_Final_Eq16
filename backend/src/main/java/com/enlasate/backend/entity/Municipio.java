package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "municipio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMunicipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado")
    private Estado estado;

    private String nombreMunicipio;
}