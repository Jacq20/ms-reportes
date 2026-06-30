package com.perfulandia.ms_reportes.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.perfulandia.ms_reportes.dto.PeriodoDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.repository.ReporteRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    /**
     * Genera un reporte de ventas, inventario o rendimiento de sucursal
     * para un periodo dado. La obtención de los datos crudos de cada
     * dominio (ventas, inventario, etc.) se delega a los microservicios
     * correspondientes; aquí se registra la solicitud de reporte ya
     * resuelta con su periodo.
     */
    public Reporte generarReporte(TipoReporte tipo, String idSucursal, PeriodoDTO periodo) {
        if (periodo == null || periodo.periodoInicio() == null || periodo.periodoFin() == null) {
            throw new RuntimeException("Debe indicar el periodo del reporte");
        }
        if (periodo.periodoInicio().isAfter(periodo.periodoFin())) {
            throw new RuntimeException("El periodo de inicio no puede ser posterior al periodo de fin");
        }

        Reporte reporte = new Reporte();
        reporte.setTipo(tipo);
        reporte.setIdSucursal(idSucursal);
        reporte.setPeriodoInicio(periodo.periodoInicio());
        reporte.setPeriodoFin(periodo.periodoFin());
        reporte.setFechaGeneracion(LocalDateTime.now());

        return reporteRepository.save(reporte);
    }
}
