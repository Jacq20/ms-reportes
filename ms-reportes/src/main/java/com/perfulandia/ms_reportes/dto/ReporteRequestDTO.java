package com.perfulandia.ms_reportes.dto;

import java.time.LocalDateTime;

import com.perfulandia.ms_reportes.model.TipoReporte;

public record ReporteRequestDTO(
    TipoReporte tipo,
    String idSucursal,
    LocalDateTime periodoInicio,
    LocalDateTime periodoFin
) {}
