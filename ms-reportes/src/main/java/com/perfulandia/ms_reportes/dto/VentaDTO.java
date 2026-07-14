package com.perfulandia.ms_reportes.dto;

public record VentaDTO(
    Long id,
    Long idUsuario,
    double total,
    String estado
) {}