package com.enlasate.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.enlasate.backend.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario_IdUsuario(Integer idUsuario);
    Optional<Cliente> findByUsuario_Email(String email);
    Page<Cliente> findByEstado(String estado, Pageable pageable);
    @Query("""
            SELECT c FROM Cliente c
            LEFT JOIN c.usuario u
            WHERE LOWER(COALESCE(u.nombre, c.nombreContacto, ''))  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(u.apellido, ''))                  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(u.email, c.emailContacto, ''))    LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Cliente> buscar(@Param("q") String q, Pageable pageable);
}

