package com.enlasate.backend.dto;

public record DomicilioResponse(
        Integer idDomicilio,
        Integer idCliente,
        String nombreCliente,
        Integer idMunicipio,
        String nombreMunicipio,
        String nombreEstado,
        String calle,
        String numero,
        String colonia,
        String codigoPostal,
        Integer idColonia
) {}