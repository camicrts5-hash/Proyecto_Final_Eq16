package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "domicilio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDomicilio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_municipio")
    private Municipio municipio;

    private String calle;
    private String numero;
    private String colonia;
    private String codigoPostal;
}