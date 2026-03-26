package com.vantryx.api.controller;

import com.vantryx.api.config.JwtService;
import com.vantryx.api.service.EmailService;
import com.vantryx.api.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/reports/inventory debería descargar Excel y enviar email")
    void shouldDownloadReportAndSendEmail() throws Exception {
        // 1. Preparamos un "falso" Excel (un array de bytes simple)
        byte[] mockExcelContent = "Contenido de Excel Simulado".getBytes();

        when(reportService.generateInventoryExcel()).thenReturn(mockExcelContent);

        // 2. Ejecución
        mockMvc.perform(get("/api/reports/inventory"))
                .andExpect(status().isOk())
                // Verificamos el Header de descarga
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_inventario_vantryx.xlsx"))
                // Verificamos el Content-Type de Excel
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))                // Verificamos que el cuerpo contenga nuestros bytes
                .andExpect(content().bytes(mockExcelContent));

        // 3. VERIFICACIÓN CRÍTICA: ¿Se llamó al servicio de email?
        // Verificamos que se llamó con el email exacto y el asunto exacto
        verify(emailService).sendEmailWithAttachment(
                eq("admin@vantryx.com"),
                eq("Alerta de Inventario - Vantryx AI"),
                any(String.class), // El mensaje puede variar un poco
                eq(mockExcelContent), // Los bytes deben ser los mismos
                eq("reporte_inventario_vantryx.xlsx")
        );
    }
}