package com.vantryx.api.service;

import com.vantryx.api.dto.StockAlertDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductService productService;

    public byte[] generateInventoryExcel() throws IOException {
        List<StockAlertDTO> alerts = productService.getInventoryAlerts();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Alertas de Inventario");

            // 1. Estilo para la cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 2. Crear cabecera
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Producto", "Stock Actual", "Mínimo", "Proveedor", "Estado", "Sugerencia"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Llenar datos
            int rowIdx = 1;
            for (StockAlertDTO alert : alerts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(alert.getProductName());
                row.createCell(1).setCellValue(alert.getCurrentStock());
                row.createCell(2).setCellValue(alert.getMinStock());
                row.createCell(3).setCellValue(alert.getSupplierName());
                row.createCell(4).setCellValue(alert.getStatus());
                row.createCell(5).setCellValue(alert.getSuggestion());
            }

            // 4. Autoajustar columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
