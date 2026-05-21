export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT';

export interface MovementResponseDTO {
  id: number;
  quantity: number;
  type: MovementType;
  reason: string;
  username: string;
  createdAt: string; // ISO string, viene como LocalDateTime de Java
}

export interface StockMovementRequestDTO {
  productId: number;
  quantity: number;
  type: MovementType;
  reason: string;
}
