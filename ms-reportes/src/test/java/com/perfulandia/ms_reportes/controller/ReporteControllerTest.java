package com.perfulandia.ms_reportes.controller;

import java.time.LocalDateTime;

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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.perfulandia.ms_reportes.dto.ReporteRequestDTO;
import com.perfulandia.ms_reportes.model.Reporte;
import com.perfulandia.ms_reportes.model.TipoReporte;
import com.perfulandia.ms_reportes.service.ReporteService;

@WebMvcTest(ReporteController.class)
@ActiveProfiles("test")
public class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void testGenerarReporte() throws Exception {
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.VENTAS,
            "suc-01",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 31, 0, 0)
        );
        Reporte generado = new Reporte(1L, TipoReporte.VENTAS, "suc-01",
            request.periodoInicio(), request.periodoFin(), LocalDateTime.now(), "Resumen de prueba");
        Mockito.when(reporteService.generarReporte(any(ReporteRequestDTO.class)))
               .thenReturn(generado);

        mockMvc.perform(post("/api/v1/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("VENTAS"));
    }

    @Test
    void testGenerarReportePeriodoInvalido() throws Exception {
        ReporteRequestDTO request = new ReporteRequestDTO(
            TipoReporte.INVENTARIO,
            "suc-01",
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        Mockito.when(reporteService.generarReporte(any(ReporteRequestDTO.class)))
               .thenThrow(new RuntimeException("El periodo de inicio no puede ser posterior al periodo de fin"));

        mockMvc.perform(post("/api/v1/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static <T> T any(Class<T> clazz) {
        return org.mockito.ArgumentMatchers.any(clazz);
    }
}