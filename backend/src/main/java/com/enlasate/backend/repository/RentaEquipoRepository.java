package com.enlasate.backend.repository;

import com.enlasate.backend.entity.RentaEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RentaEquipoRepository extends JpaRepository<RentaEquipo, Integer> {
    Optional<RentaEquipo> findByCliente_IdCliente(Integer idCliente);
}