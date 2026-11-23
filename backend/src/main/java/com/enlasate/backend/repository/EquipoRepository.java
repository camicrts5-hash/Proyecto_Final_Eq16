package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Equipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    Page<Equipo> findByEstadoEquipo(String estadoEquipo, Pageable pageable);
    long countByEstadoEquipo(String estadoEquipo);
}