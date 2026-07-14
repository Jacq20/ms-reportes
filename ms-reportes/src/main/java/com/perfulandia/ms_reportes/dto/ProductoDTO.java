package com.perfulandia.ms_reportes.dto;

public record ProductoDTO(
    Long id,
    String nombre,
    String categoria,
    double precio
) {
}
