package com.enlasate.backend.service;

import com.enlasate.backend.dto.NotificacionRequest;
import com.enlasate.backend.dto.NotificacionResponse;
import com.enlasate.backend.entity.Notificacion;
import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.repository.NotificacionRepository;
import com.enlasate.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> listar(Pageable pageable) {
        return notificacionRepository.findAll(pageable)
                .map(this::convertirResponse);
    }

    @Transactional(readOnly = true)
    public NotificacionResponse buscarPorId(Integer id) {

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la notificación con ID: " + id));

        return convertirResponse(notificacion);
    }

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> buscarPorUsuario(
            Integer idUsuario, Pageable pageable) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new EntityNotFoundException(
                    "No se encontró el usuario con ID: " + idUsuario);
        }

        return notificacionRepository
                .findByUsuario_IdUsuario(idUsuario, pageable)
                .map(this::convertirResponse);
    }

    public NotificacionResponse registrar(NotificacionRequest request) {

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el usuario"));

        Notificacion notificacion = new Notificacion();

        notificacion.setUsuario(usuario);
        notificacion.setMensaje(request.mensaje());
        notificacion.setFechaEnvio(
                request.fechaEnvio() != null
                        ? request.fechaEnvio()
                        : LocalDateTime.now()
        );
        notificacion.setTipo(request.tipo());

        return convertirResponse(
                notificacionRepository.save(notificacion));
    }

    public NotificacionResponse actualizar(
            Integer id, NotificacionRequest request) {

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la notificación con ID: " + id));

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el usuario"));

        notificacion.setUsuario(usuario);
        notificacion.setMensaje(request.mensaje());
        notificacion.setFechaEnvio(
                request.fechaEnvio() != null
                        ? request.fechaEnvio()
                        : notificacion.getFechaEnvio()
        );
        notificacion.setTipo(request.tipo());

        return convertirResponse(
                notificacionRepository.save(notificacion));
    }

    public void eliminar(Integer id) {

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la notificación con ID: " + id));

        notificacionRepository.delete(notificacion);
    }

    private NotificacionResponse convertirResponse(
            Notificacion notificacion) {

        return new NotificacionResponse(
                notificacion.getIdNotificacion(),
                notificacion.getUsuario().getIdUsuario(),
                notificacion.getUsuario().getNombre() + " "
                        + notificacion.getUsuario().getApellido(),
                notificacion.getUsuario().getEmail(),
                notificacion.getMensaje(),
                notificacion.getFechaEnvio(),
                notificacion.getTipo()
        );
    }
}