package com.perfulandia.ms_reportes.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class RestTemplateConfigTest {
    
    @Test
    void testRestTemplateBeanNoEsNulo() {
        // Given
        RestTemplateConfig config = new RestTemplateConfig();

        // When
        RestTemplate restTemplate = config.restTemplate();

        // Then
        assertNotNull(restTemplate);
    }
}
