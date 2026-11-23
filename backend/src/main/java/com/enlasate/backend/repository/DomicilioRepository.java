package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DomicilioRepository extends JpaRepository<Domicilio, Integer> {
    List<Domicilio> findByCliente_IdCliente(Integer idCliente);
}