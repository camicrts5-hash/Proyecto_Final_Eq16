package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Optional<Empleado> findByUsuario_IdUsuario(Integer idUsuario);
}