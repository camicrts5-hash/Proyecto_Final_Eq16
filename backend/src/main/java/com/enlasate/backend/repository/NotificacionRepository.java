package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    Page<Notificacion> findByUsuario_IdUsuario(Integer idUsuario, Pageable pageable);
}