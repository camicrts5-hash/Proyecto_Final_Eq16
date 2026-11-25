package com.enlasate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Datos de contacto propios (clientes sin cuenta en la app)
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan")
    private Plan plan;

    private LocalDate fechaAlta;
    private String estado;

    // --- Flujo de instalación (V6 / V8) ---
    private String codigoActivacion;
    private LocalDateTime codigoExpira;
    private LocalDateTime fechaInstalacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<PagoTotal> pagos;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Domicilio> domicilios;
}