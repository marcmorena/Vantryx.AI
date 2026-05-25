package com.vantryx.api.controller;

import com.vantryx.api.service.EmailService;
import com.vantryx.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final EmailService emailService; // 1. <--- INYECTAMOS EL SERVICIO DE EMAIL

    @GetMapping("/inventory")
    public ResponseEntity<byte[]> downloadInventoryReport() throws IOException {
        // Generamos los datos del Excel una sola vez
        byte[] report = reportService.generateInventoryExcel();

        emailService.sendEmailWithAttachment(
                "admin@vantryx.com",
                "Alerta de Inventario - Vantryx AI",
                "Hola, se adjunta el reporte de stock crítico generado hoy.",
                report,
                "reporte_inventario_vantryx.xlsx"
        );

        String fileName = "reporte_inventario_vantryx.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(report);
    }
}
