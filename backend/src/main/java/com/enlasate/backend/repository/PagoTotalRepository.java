package com.enlasate.backend.repository;

import com.enlasate.backend.entity.PagoTotal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PagoTotalRepository extends JpaRepository<PagoTotal, Integer> {

    Page<PagoTotal> findByCliente_IdCliente(
        Integer idCliente,
        Pageable pageable
    );

    Page<PagoTotal> findByCliente_Usuario_Email(
        String email,
        Pageable pageable
    );

    Page<PagoTotal> findByMesCorrespondiente(
        String mes,
        Pageable pageable
    );

    boolean existsByCliente_IdClienteAndMesCorrespondiente(
        Integer idCliente,
        String mes
    );

    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM PagoTotal p
        WHERE p.cliente.idCliente = :idCliente
    """)
    BigDecimal totalPagadoPorCliente(
        @Param("idCliente") Integer idCliente
    );

    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM PagoTotal p
        WHERE p.mesCorrespondiente = :mes
    """)
    BigDecimal totalCobradoEnMes(
        @Param("mes") String mes
    );
}