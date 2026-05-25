package com.vantryx.api.service;

import com.vantryx.api.dto.StockAlertDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Debería generar un array de bytes no vacío cuando hay alertas")
    void shouldGenerateExcelBytes() throws IOException {
        // 1. Preparar datos simulados
        StockAlertDTO alert = StockAlertDTO.builder()
                .productName("Laptop Gamer")
                .currentStock(2)
                .minStock(5)
                .supplierName("Tech Global")
                .status("CRITICAL")
                .suggestion("Comprar 10 unidades")
                .build();

        when(productService.getInventoryAlerts()).thenReturn(List.of(alert));

        // 2. Ejecutar
        byte[] excelContent = reportService.generateInventoryExcel();

        // 3. Verificar
        assertNotNull(excelContent, "El reporte no debería ser nulo");
        assertTrue(excelContent.length > 0, "El reporte debería tener contenido");

        // Un archivo .xlsx siempre empieza con los mismos bytes (PK..) porque es un ZIP
        // Esto confirma que Apache POI hizo su trabajo
        assertEquals('P', (char) excelContent[0]);
        assertEquals('K', (char) excelContent[1]);
    }

    @Test
    @DisplayName("Debería generar el Excel incluso si la lista de alertas está vacía")
    void shouldGenerateExcelEvenIfEmpty() throws IOException {
        when(productService.getInventoryAlerts()).thenReturn(List.of());

        byte[] excelContent = reportService.generateInventoryExcel();

        assertNotNull(excelContent);
        assertTrue(excelContent.length > 0); // El Excel tendrá al menos la cabecera
    }
}