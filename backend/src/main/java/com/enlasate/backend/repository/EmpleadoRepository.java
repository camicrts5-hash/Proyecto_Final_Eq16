package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Empleado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    Optional<Empleado> findByUsuario_IdUsuario(Integer idUsuario);

    Optional<Empleado> findByUsuario_Email(String email);

    // Trae usuario y su rol en la misma consulta
    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rol"})
    List<Empleado> findAll();
}
