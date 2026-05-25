package com.vantryx.api.task;

import com.vantryx.api.service.EmailService;
import com.vantryx.api.service.ProductService;
import com.vantryx.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Esto nos permite usar 'log.info' para ver mensajes en la consola
public class InventoryTaskService {

    private final ProductService productService;
    private final ReportService reportService;
    private final EmailService emailService;

    /**
     * Se ejecuta automáticamente cada 1 minuto (60.000 ms) para pruebas.
     * En producción usarías una expresión Cron como: "0 0 9 * * MON" (Lunes a las 9am)
     */
    @Scheduled(cron = "0 0 9 * * MON") // Todos los lunes a las 9 AM
    public void runAutomaticInventoryCheck() {
        var alerts = productService.getInventoryAlerts();

        if (!alerts.isEmpty()) {
            try {
                // 1. Generamos el reporte en el momento
                byte[] excelReport = reportService.generateInventoryExcel();

                // 2. Enviamos el email
                emailService.sendEmailWithAttachment(
                        "admin@vantryx.com",
                        "🚨 Alerta de Inventario Crítico - Vantryx AI",
                        "Hola, se adjunta el reporte de productos que requieren reposición inmediata.",
                        excelReport,
                        "reporte_compra.xlsx"
                );
                log.info("📧 Email de alerta enviado con éxito.");
            } catch (Exception e) {
                log.error("❌ Fallo al generar o enviar el reporte automático: " + e.getMessage());
            }
        }
    }
}
