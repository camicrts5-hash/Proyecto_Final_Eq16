package com.enlasate.backend.exception;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> crearRespuesta(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    // 400 - Errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Los datos enviados no son válidos");

        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    // 401 - Credenciales incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> manejarCredenciales(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos",
                request
        );
    }

    // 403 - Acceso prohibido
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> manejarAccesoDenegado(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para acceder a este recurso",
                request
        );
    }

    // 404 - Recurso no encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.NOT_FOUND,
                "El recurso solicitado no existe",
                request
        );
    }

    // 404 y otros códigos definidos mediante ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> manejarStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        return crearRespuesta(
                status,
                ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(),
                request
        );
    }

    // 422 - Datos o solicitudes que no pueden procesarse
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarArgumento(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage() != null ? ex.getMessage() : "Los datos no pueden procesarse",
                request
        );
    }

    // 409 - Conflictos con los datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                "Existe un conflicto con los datos enviados",
                request
        );
    }

    // 500 - Error interno del servidor
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                request
        );
    }
}