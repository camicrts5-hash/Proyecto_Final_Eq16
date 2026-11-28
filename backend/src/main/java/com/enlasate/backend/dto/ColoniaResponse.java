package com.enlasate.backend.dto;

public record ColoniaResponse(
        Integer idColonia,
        Integer idMunicipio,
        String nombreMunicipio,
        String nombreColonia
) {}
