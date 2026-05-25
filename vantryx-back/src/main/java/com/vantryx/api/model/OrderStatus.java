package com.vantryx.api.model;

public enum OrderStatus {
    DRAFT,      // Lo estamos preparando
    ORDERED,    // Ya se lo enviamos al proveedor
    RECEIVED,   // ¡Llegó al almacén! (Aquí es donde se sumará el stock)
    CANCELLED   // El pedido se anuló
}
