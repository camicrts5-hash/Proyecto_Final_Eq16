package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Colonia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColoniaRepository extends JpaRepository<Colonia, Integer> {

    List<Colonia> findByMunicipio_IdMunicipioOrderByNombreColoniaAsc(Integer idMunicipio);

    boolean existsByMunicipio_IdMunicipioAndNombreColoniaIgnoreCase(
            Integer idMunicipio, String nombreColonia);
}
