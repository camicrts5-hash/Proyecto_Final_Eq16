package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {
    List<Municipio> findByEstado_IdEstado(Integer idEstado);
}