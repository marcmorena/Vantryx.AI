package com.vantryx.api.service;

import com.vantryx.api.dto.DashboardDTO;
import com.vantryx.api.dto.ProductDTO;
import com.vantryx.api.mapper.ProductMapper;
import com.vantryx.api.model.OrderStatus;
import com.vantryx.api.model.Product;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.PurchaseOrderRepository;
import com.vantryx.api.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PurchaseOrderRepository purchaseRepository;
    private final ProductMapper productMapper;

    public DashboardDTO getDashboardData() {
        // 1. Métricas de Inventario
        long totalProds = productRepository.count();
        BigDecimal invValue = productRepository.calculateInventoryValue();

        // Buscamos productos bajo el mínimo (Seguro ya tienes un método así)
        List<Product> lowStockEntities = productRepository.findByCurrentStockLessThanColumnMinStock();
        List<ProductDTO> lowStockDTOs = lowStockEntities.stream()
                .map(productMapper::toDTO)
                .toList();

        // 2. Métricas Financieras
        BigDecimal revenue = saleRepository.sumAllRevenue();
        if (revenue == null) revenue = BigDecimal.ZERO;

        BigDecimal investment = purchaseRepository.sumTotalByStatus(OrderStatus.RECEIVED);
        if (investment == null) investment = BigDecimal.ZERO;

        return DashboardDTO.builder()
                .totalProducts(totalProds)
                .totalInventoryValue(invValue != null ? invValue : BigDecimal.ZERO)
                .lowStockProducts(lowStockDTOs)
                .criticalAlertsCount(lowStockDTOs.size())
                .totalRevenue(revenue)
                .totalInvestment(investment)
                .netProfit(revenue.subtract(investment))
                .build();
    }
}
