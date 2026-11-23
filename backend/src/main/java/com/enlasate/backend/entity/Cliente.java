package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cliente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan")
    private Plan plan;

    private LocalDate fechaAlta;
    private String estado;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<PagoTotal> pagos;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Domicilio> domicilios;
}