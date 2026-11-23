package com.enlasate.backend.repository;

import com.enlasate.backend.entity.SoporteTecnico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoporteTecnicoRepository extends JpaRepository<SoporteTecnico, Integer> {
    Page<SoporteTecnico> findByEstadoTicket(String estado, Pageable pageable);
    Page<SoporteTecnico> findByCliente_IdCliente(Integer idCliente, Pageable pageable);
    Page<SoporteTecnico> findByEmpleado_IdEmpleado(Integer idEmpleado, Pageable pageable);
}