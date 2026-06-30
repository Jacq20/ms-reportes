package com.perfulandia.ms_reportes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.perfulandia.ms_reportes.dto.PeriodoDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.service.ReporteService;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @PostMapping
    public Reporte generarReporte(@RequestParam TipoReporte tipo,
                                   @RequestParam String idSucursal,
                                   @RequestBody PeriodoDTO periodo) {
        return reporteService.generarReporte(tipo, idSucursal, periodo);
    }
}
