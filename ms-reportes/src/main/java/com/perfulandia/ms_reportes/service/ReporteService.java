package com.perfulandia.ms_reportes.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.perfulandia.ms_reportes.dto.ReporteRequestDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.repository.ReporteRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    public Reporte generarReporte(ReporteRequestDTO request) {
        if (request.periodoInicio() == null || request.periodoFin() == null) {
            throw new RuntimeException("Debe indicar el periodo del reporte");
        }
        if (request.periodoInicio().isAfter(request.periodoFin())) {
            throw new RuntimeException("El periodo de inicio no puede ser posterior al periodo de fin");
        }

        Reporte reporte = new Reporte();
        reporte.setTipo(request.tipo());
        reporte.setIdSucursal(request.idSucursal());
        reporte.setPeriodoInicio(request.periodoInicio());
        reporte.setPeriodoFin(request.periodoFin());
        reporte.setFechaGeneracion(LocalDateTime.now());

        return reporteRepository.save(reporte);
    }
}