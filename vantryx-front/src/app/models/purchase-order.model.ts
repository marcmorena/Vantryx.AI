export enum OrderStatus {
  DRAFT = 'DRAFT',
  ORDERED = 'ORDERED',
  RECEIVED = 'RECEIVED'
}

export interface PurchaseOrderRequestDTO {
  supplierId: number;
  items: OrderItemRequestDTO[];
}

export interface OrderItemRequestDTO {
  productId: number;
  quantity: number;
  priceAtPurchase: number;
}

export interface PurchaseOrderResponseDTO {
  id: number;
  supplierName: string;
  username: string;
  orderDate: string;
  deliveryDate?: string;
  status: OrderStatus;
  totalAmount: number;
  items: PurchaseOrderItemResponseDTO[];
}

export interface PurchaseOrderItemResponseDTO {
  productId: number;
  productName: string;
  quantity: number;
  priceAtPurchase: number;
}