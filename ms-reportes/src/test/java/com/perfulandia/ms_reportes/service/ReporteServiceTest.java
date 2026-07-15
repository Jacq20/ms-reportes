package com.perfulandia.ms_reportes.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.perfulandia.ms_reportes.dto.ItemInventarioDTO;
import com.perfulandia.ms_reportes.dto.ProductoDTO;
import com.perfulandia.ms_reportes.dto.ReporteRequestDTO;
import com.perfulandia.ms_reportes.dto.VentaDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.repository.ReporteRepository;


@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reporteService, "msInventarioUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(reporteService, "msCatalogoUrl",   "http://localhost:8082");
        ReflectionTestUtils.setField(reporteService, "msVentasUrl",     "http://localhost:8084");
    }

    //VENTAS

    @Test
    void testGenerarReporteVentas() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.VENTAS, "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        VentaDTO[] ventas = {
            new VentaDTO(1L, 10L, 50000.0, "COMPLETADA"),
            new VentaDTO(2L, 11L, 30000.0, "COMPLETADA")
        };
        Reporte guardado = new Reporte(1L, TipoReporte.VENTAS, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "REPORTE DE VENTAS | Sucursal: suc-01 | Total de ventas: 2 | Monto total: $80000.00");
        when(restTemplate.getForObject(contains("/ventas/historial/"), eq(VentaDTO[].class)))
            .thenReturn(ventas);
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoReporte.VENTAS, resultado.getTipo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testGenerarReporteVentasListaVacia() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.VENTAS, "suc-99",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        when(restTemplate.getForObject(contains("/ventas/historial/"), eq(VentaDTO[].class)))
            .thenReturn(new VentaDTO[0]);
        Reporte guardado = new Reporte(1L, TipoReporte.VENTAS, "suc-99",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "No se encontraron ventas para la sucursal suc-99");
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getResumen().contains("No se encontraron ventas"));
    }

    //INVENTARIO
    @Test
    void testGenerarReporteInventario() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.INVENTARIO, "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        ItemInventarioDTO[] items = {
            new ItemInventarioDTO(1L, 10L, 1L, 5, 10),  // bajo stock
            new ItemInventarioDTO(2L, 11L, 1L, 20, 5)   // stock normal
        };
        Reporte guardado = new Reporte(1L, TipoReporte.INVENTARIO, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "REPORTE DE INVENTARIO | Sucursal: suc-01 | Total productos: 2 | Bajo stock: 1");
        when(restTemplate.getForObject(contains("/inventario/sucursal/"), eq(ItemInventarioDTO[].class)))
            .thenReturn(items);
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoReporte.INVENTARIO, resultado.getTipo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testGenerarReporteInventarioListaVacia() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.INVENTARIO, "suc-99",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        when(restTemplate.getForObject(contains("/inventario/sucursal/"), eq(ItemInventarioDTO[].class)))
            .thenReturn(null);
        Reporte guardado = new Reporte(1L, TipoReporte.INVENTARIO, "suc-99",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "No se encontraron items de inventario para la sucursal suc-99");
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getResumen().contains("No se encontraron items"));
    }

    //RENDIMIENTO_SUCURSAL
    @Test
    void testGenerarReporteRendimiento() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.RENDIMIENTO_SUCURSAL, "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        ProductoDTO[] productos = {
            new ProductoDTO(1L, "Perfume A", "Perfumería", 15000.0),
            new ProductoDTO(2L, "Crema B",  "Cosméticos",  5000.0)
        };
        Reporte guardado = new Reporte(1L, TipoReporte.RENDIMIENTO_SUCURSAL, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "REPORTE DE RENDIMIENTO | Sucursal: suc-01 | Productos en catalogo: 2 | Precio promedio: $10000.00");
        when(restTemplate.getForObject(contains("/productos"), eq(ProductoDTO[].class)))
            .thenReturn(productos);
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoReporte.RENDIMIENTO_SUCURSAL, resultado.getTipo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void testGenerarReporteRendimientoListaVacia() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.RENDIMIENTO_SUCURSAL, "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        when(restTemplate.getForObject(contains("/productos"), eq(ProductoDTO[].class)))
            .thenReturn(null);
        Reporte guardado = new Reporte(1L, TipoReporte.RENDIMIENTO_SUCURSAL, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "No se encontraron productos en el catalogo");
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getResumen().contains("No se encontraron productos"));
    }

    //ERROR DE COMUNICACION
    @Test
    void testGenerarReporteRestClientException() {
        // Given — simula que ms-ventas no responde
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.VENTAS, "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 23, 59)
        );
        when(restTemplate.getForObject(contains("/ventas/historial/"), eq(VentaDTO[].class)))
            .thenThrow(new RestClientException("Connection refused"));
        Reporte guardado = new Reporte(1L, TipoReporte.VENTAS, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(),
            "No se pudo obtener datos del microservicio: Connection refused");
        when(reporteRepository.save(any(Reporte.class))).thenReturn(guardado);

        // When — NO debe lanzar excepción, guarda el error en resumen
        Reporte resultado = reporteService.generarReporte(request);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getResumen().contains("No se pudo obtener datos"));
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    //VALIDACIONES DE PERIODO

    @Test
    void testGenerarReporteSinPeriodo() {
        // Given
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.INVENTARIO, "suc-01", null, null
        );

        // When
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> reporteService.generarReporte(request));

        // Then
        assertEquals("Debe indicar el periodo del reporte", ex.getMessage());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }

    @Test
    void testGenerarReportePeriodoInvalido() {
        // Given — inicio posterior al fin
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.RENDIMIENTO_SUCURSAL, "suc-01",
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 1, 1, 0, 0)
        );

        // When
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> reporteService.generarReporte(request));

        // Then
        assertEquals("El periodo de inicio no puede ser posterior al periodo de fin", ex.getMessage());
        verify(reporteRepository, never()).save(any(Reporte.class));
    }
}
