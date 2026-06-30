package com.perfulandia.ms_reportes.controller;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfulandia.ms_reportes.dto.PeriodoDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.service.ReporteService;

@WebMvcTest(ReporteController.class)
@ActiveProfiles("test")
public class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockitoBean
    private ReporteService reporteService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGenerarReporte() throws Exception {
        PeriodoDTO periodo = new PeriodoDTO(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 0, 0));
        Reporte generado = new Reporte(1L, TipoReporte.VENTAS, "suc-01", periodo.periodoInicio(), periodo.periodoFin(), LocalDateTime.now());
        Mockito.when(reporteService.generarReporte(eq(TipoReporte.VENTAS), eq("suc-01"), any(PeriodoDTO.class)))
                .thenReturn(generado);

        mockMvc.perform(post("/api/v1/reportes")
                        .param("tipo", "VENTAS")
                        .param("idSucursal", "suc-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(periodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("VENTAS"));
    }

    @Test
    void testGenerarReportePeriodoInvalido() throws Exception {
        PeriodoDTO periodo = new PeriodoDTO(LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
        Mockito.when(reporteService.generarReporte(eq(TipoReporte.INVENTARIO), eq("suc-01"), any(PeriodoDTO.class)))
                .thenThrow(new RuntimeException("El periodo de inicio no puede ser posterior al periodo de fin"));

        mockMvc.perform(post("/api/v1/reportes")
                        .param("tipo", "INVENTARIO")
                        .param("idSucursal", "suc-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(periodo)))
                .andExpect(status().isBadRequest());
    }
}
