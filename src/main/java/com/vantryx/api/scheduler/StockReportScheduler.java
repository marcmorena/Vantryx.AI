package com.vantryx.api.scheduler;

import com.vantryx.api.model.Role;
import com.vantryx.api.service.EmailService;
import com.vantryx.api.service.ReportService;
import com.vantryx.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockReportScheduler {

    private final ReportService reportService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Cron: "0 0 8 * * MON" -> Lunes a las 08:00:00
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyReportToAdmins() {
        log.info("Iniciando generación de reporte semanal de stock crítico...");

        try {
            // 1. Generar el Excel (que ahora usa la consulta optimizada)
            byte[] reportFile = reportService.generateInventoryExcel();

            if (reportFile == null || reportFile.length == 0) {
                log.info("No hay productos con stock crítico. No se enviará correo.");
                return;
            }

            // 2. Obtener emails de administradores (Suponiendo que tienes Role en User)
            // Si no quieres buscar en BD, puedes poner un email fijo aquí
            userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .forEach(admin -> {
                        String subject = "⚠️ REPORTE SEMANAL: Stock Crítico - " + LocalDate.now();
                        String body = "Hola " + admin.getUsername() + ",\n\nAdjuntamos el reporte de productos " +
                                "que han alcanzado el límite de stock mínimo.\n\nSistema Vantryx.";

                        emailService.sendEmailWithAttachment(
                                admin.getEmail(),
                                subject,
                                body,
                                reportFile,
                                "Reporte_Stock_" + LocalDate.now() + ".xlsx"
                        );
                    });

            log.info("Reportes enviados correctamente.");

        } catch (IOException e) {
            log.error("Error al generar el archivo de reporte", e);
        }
    }
}