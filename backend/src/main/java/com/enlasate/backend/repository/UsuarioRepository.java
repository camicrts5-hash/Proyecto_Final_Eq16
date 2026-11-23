package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsuario(String usuario);
 boolean existsByEmail(String email);
    boolean existsByUsuario(String usuario);
    Page<Usuario> findByRol_NombreRol(String nombreRol, Pageable pageable);
    Page<Usuario> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
            String nombre, String apellido, Pageable pageable);
}
