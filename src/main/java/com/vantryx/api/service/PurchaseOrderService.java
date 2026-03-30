package com.vantryx.api.service;

import com.vantryx.api.dto.*;
import com.vantryx.api.exception.ResourceNotFoundException;
import com.vantryx.api.mapper.PurchaseOrderMapper;
import com.vantryx.api.model.*;
import com.vantryx.api.repository.ProductRepository;
import com.vantryx.api.repository.PurchaseOrderRepository;
import com.vantryx.api.repository.SupplierRepository;
import com.vantryx.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final StockMovementService stockMovementService; // Para actualizar el stock al recibir
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final ProductService productService;

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrderRequestDTO dto) {
        // 1. Auditoría: ¿Quién está comprando?
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validar Proveedor
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // 3. Crear la cabecera de la Orden
        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .user(user)
                .status(OrderStatus.ORDERED)
                .orderDate(LocalDateTime.now())
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();

        // 4. Procesar los ítems y calcular el total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDto.getProductId()));

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .priceAtPurchase(itemDto.getPriceAtPurchase())
                    .build();

            // Sumamos al total: cantidad * precio
            BigDecimal subtotal = itemDto.getPriceAtPurchase().multiply(new BigDecimal(itemDto.getQuantity()));
            total = total.add(subtotal);

            order.getItems().add(item);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional
    public void receiveOrder(Long orderId) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new RuntimeException("Esta orden ya ha sido recibida anteriormente");
        }

        // 1. Cambiar estado y fecha de entrega
        order.setStatus(OrderStatus.RECEIVED);
        order.setDeliveryDate(LocalDateTime.now());

        // 2. Por cada ítem, registramos un movimiento de ENTRADA en el inventario
        for (PurchaseOrderItem item : order.getItems()) {
            StockMovementDTO movement = StockMovementDTO.builder()
                    .productId(item.getProduct().getId())
                    .quantity(item.getQuantity())
                    .type(com.vantryx.api.model.MovementType.IN)
                    .reason("Recepción de Orden de Compra #" + order.getId())
                    .build();

            // Reutilizamos tu lógica de auditoría y validación
            stockMovementService.registerMovement(movement);
        }

        orderRepository.save(order);
    }

    public List<PurchaseOrderResponseDTO> getPendingOrders() {
        // Definimos qué estados consideramos "pendientes"
        List<OrderStatus> pendingStatuses = List.of(OrderStatus.DRAFT, OrderStatus.ORDERED);

        return orderRepository.findByStatusInOrderByCreatedAtDesc(pendingStatuses)
                .stream()
                .map(purchaseOrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PurchaseOrderResponseDTO> generateSuggestedOrders() {
        // 1. Obtenemos las alertas de stock que ya programaste
        List<StockAlertDTO> alerts = productService.getInventoryAlerts();

        // 2. Filtramos solo las que necesitan pedido (CRÍTICO o STOCK_BAJO)
        // y las agrupamos por nombre de proveedor (o ID)
        Map<String, List<StockAlertDTO>> alertsBySupplier = alerts.stream()
                .filter(a -> !"OK".equals(a.getStatus()))
                .collect(Collectors.groupingBy(StockAlertDTO::getSupplierName));

        List<PurchaseOrder> createdOrders = new ArrayList<>();

        // 3. Por cada proveedor, creamos una orden en borrador (DRAFT)
        alertsBySupplier.forEach((supplierName, supplierAlerts) -> {
            // Buscamos el proveedor y el usuario actual (auditoría)
            Supplier supplier = supplierRepository.findByName(supplierName)
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + supplierName));

            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(currentUsername).get();

            PurchaseOrder order = PurchaseOrder.builder()
                    .supplier(supplier)
                    .user(user)
                    .status(OrderStatus.DRAFT)
                    .orderDate(LocalDateTime.now())
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            BigDecimal total = BigDecimal.ZERO;
            for (StockAlertDTO alert : supplierAlerts) {
                Product product = productRepository.findById(alert.getProductId()).get();

                // Sugerencia simple: pedir lo necesario para llegar al doble del minStock
                int quantityToOrder = (product.getMinStock() * 2) - product.getCurrentStock();

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .purchaseOrder(order)
                        .product(product)
                        .quantity(quantityToOrder)
                        .priceAtPurchase(product.getPurchasePrice())
                        .build();

                total = total.add(product.getPurchasePrice().multiply(BigDecimal.valueOf(quantityToOrder)));
                order.getItems().add(item);
            }

            order.setTotalAmount(total);
            createdOrders.add(orderRepository.save(order));
        });

        return createdOrders.stream()
                .map(purchaseOrderMapper::toDTO)
                .collect(Collectors.toList());
    }
}
