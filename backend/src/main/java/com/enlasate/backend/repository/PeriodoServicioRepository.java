package com.enlasate.backend.repository;

import com.enlasate.backend.entity.PeriodoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeriodoServicioRepository
        extends JpaRepository<PeriodoServicio, Integer> {

    List<PeriodoServicio> findByCliente_IdClienteOrderByFechaInicioAsc(Integer idCliente);

    Optional<PeriodoServicio> findFirstByCliente_IdClienteAndFechaFinIsNullOrderByFechaInicioDesc(
            Integer idCliente
    );
}
