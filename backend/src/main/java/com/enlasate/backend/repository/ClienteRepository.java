package com.enlasate.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.enlasate.backend.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario_IdUsuario(Integer idUsuario);
    Optional<Cliente> findByUsuario_Email(String email);
    Page<Cliente> findByEstado(String estado, Pageable pageable);
    @Query("""
            SELECT c FROM Cliente c
            LEFT JOIN c.usuario u
            WHERE LOWER(COALESCE(u.nombre, c.nombreContacto, ''))  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(u.apellido, ''))                  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(u.email, c.emailContacto, ''))    LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Cliente> buscar(@Param("q") String q, Pageable pageable);

    // Filtros combinables del panel de clientes
    @Query("""
            SELECT DISTINCT c FROM Cliente c
            LEFT JOIN c.usuario u
            LEFT JOIN c.domicilios d
            LEFT JOIN d.municipio m
            LEFT JOIN m.estado e
            WHERE (:q IS NULL OR :q = ''
                   OR LOWER(COALESCE(u.nombre, c.nombreContacto, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.apellido, ''))                  LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(u.email, c.emailContacto, ''))    LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:estado IS NULL OR :estado = '' OR c.estado = :estado)
              AND (:conCuenta IS NULL
                   OR (:conCuenta = true  AND c.usuario IS NOT NULL)
                   OR (:conCuenta = false AND c.usuario IS NULL))
              AND (:idEstado IS NULL OR e.idEstado = :idEstado)
              AND (:idMunicipio IS NULL OR m.idMunicipio = :idMunicipio)
              AND (:colonia IS NULL OR :colonia = ''
                   OR LOWER(d.colonia) LIKE LOWER(CONCAT('%', :colonia, '%')))
              AND (:pagoMes IS NULL
                   OR (:pagoMes = true AND EXISTS (
                        SELECT 1 FROM PagoTotal p
                         WHERE p.cliente = c
                           AND p.mesCorrespondiente = :mes
                           AND p.estadoPago = 'Completado'))
                   OR (:pagoMes = false AND NOT EXISTS (
                        SELECT 1 FROM PagoTotal p
                         WHERE p.cliente = c
                           AND p.mesCorrespondiente = :mes
                           AND p.estadoPago = 'Completado')))
            """)
    Page<Cliente> filtrar(
            @Param("q") String q,
            @Param("estado") String estado,
            @Param("conCuenta") Boolean conCuenta,
            @Param("idEstado") Integer idEstado,
            @Param("idMunicipio") Integer idMunicipio,
            @Param("colonia") String colonia,
            @Param("pagoMes") Boolean pagoMes,
            @Param("mes") String mes,
            Pageable pageable);

    // Agrupacion de clientes por municipio y colonia (via domicilio)
    @Query("""
            SELECT m.nombreMunicipio, d.colonia, COUNT(c)
            FROM Cliente c
            JOIN c.domicilios d
            JOIN d.municipio m
            GROUP BY m.nombreMunicipio, d.colonia
            ORDER BY m.nombreMunicipio, d.colonia
            """)
    java.util.List<Object[]> agruparPorZona();
}

