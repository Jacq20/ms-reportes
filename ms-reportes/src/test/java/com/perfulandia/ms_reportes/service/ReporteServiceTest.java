package com.perfulandia.ms_reportes.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.perfulandia.ms_reportes.dto.PeriodoDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.repository.ReporteRepository;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @InjectMocks
    private ReporteService reporteService;

    @Test
    void testGenerarReporteVentas() {
        // Given
        PeriodoDTO periodo = new PeriodoDTO(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 23, 59));
        Reporte guardado = new Reporte(1L, TipoReporte.VENTAS, "suc-01", periodo.periodoInicio(), periodo.periodoFin(), LocalDateTime.now());
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(TipoReporte.VENTAS, "suc-01", periodo);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoReporte.VENTAS, resultado.getTipo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testGenerarReporteSinPeriodo() {
        // When
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reporteService.generarReporte(TipoReporte.INVENTARIO, "suc-01", null));

        // Then
        assertEquals("Debe indicar el periodo del reporte", exception.getMessage());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void testGenerarReportePeriodoInvalido() {
        // Given
        PeriodoDTO periodo = new PeriodoDTO(LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));

        // When
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reporteService.generarReporte(TipoReporte.RENDIMIENTO_SUCURSAL, "suc-01", periodo));

        // Then
        assertEquals("El periodo de inicio no puede ser posterior al periodo de fin", exception.getMessage());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }
}
