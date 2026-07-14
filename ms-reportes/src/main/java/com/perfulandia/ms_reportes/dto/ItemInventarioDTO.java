package com.perfulandia.ms_reportes.dto;

public record ItemInventarioDTO (
    Long id,
    Long idProducto,
    Long idSucursal,
    int cantidad,
    int stockMinimo  
) {}
