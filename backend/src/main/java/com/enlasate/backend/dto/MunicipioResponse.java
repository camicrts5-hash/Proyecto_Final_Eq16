package com.enlasate.backend.dto;

public record MunicipioResponse(
        Integer idMunicipio,
        Integer idEstado,
        String nombreEstado,
        String nombreMunicipio
) {}