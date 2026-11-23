package com.enlasate.backend.repository;

import com.enlasate.backend.entity.CompraEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompraEquipoRepository extends JpaRepository<CompraEquipo, Integer> {
    Optional<CompraEquipo> findByCliente_IdCliente(Integer idCliente);
}