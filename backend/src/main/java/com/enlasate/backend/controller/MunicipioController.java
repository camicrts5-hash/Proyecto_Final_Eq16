package com.enlasate.backend.controller;

import com.enlasate.backend.dto.MunicipioResponse;
import com.enlasate.backend.service.MunicipioService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/municipios")
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<List<MunicipioResponse>> listar() {
        return ResponseEntity.ok(municipioService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<MunicipioResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(municipioService.buscarPorId(id));
    }

    @GetMapping("/estado/{idEstado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<List<MunicipioResponse>> buscarPorEstado(
            @PathVariable Integer idEstado) {
        return ResponseEntity.ok(
                municipioService.buscarPorEstado(idEstado));
    }
}