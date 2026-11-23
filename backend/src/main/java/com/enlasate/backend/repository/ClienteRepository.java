package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario_IdUsuario(Integer idUsuario);
    Optional<Cliente> findByUsuario_Email(String email);
    Page<Cliente> findByEstado(String estado, Pageable pageable);
}