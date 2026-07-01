package com.perfulandia.ms_reportes.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.perfulandia.ms_reportes.dto.ReporteRequestDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.repository.ReporteRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    @Test
    void testGenerarReporteVentas() {
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.VENTAS,
            "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        Reporte guardado = new Reporte(1L, TipoReporte.VENTAS, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now());
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        Reporte resultado = reporteService.generarReporte(request);

        assertNotNull(resultado);
        assertEquals(TipoReporte.VENTAS, resultado.getTipo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testGenerarReporteSinPeriodo() {
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.INVENTARIO, "suc-01", null, null
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> reporteService.generarReporte(request));

        assertEquals("Debe indicar el periodo del reporte", ex.getMessage());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void testGenerarReportePeriodoInvalido() {
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.RENDIMIENTO_SUCURSAL,
            "suc-01",
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 1, 1, 0, 0)
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> reporteService.generarReporte(request));

        assertEquals("El periodo de inicio no puede ser posterior al periodo de fin", ex.getMessage());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }
}
