export interface SaleDTO {
  id?: number;
  productId: number;
  productName?: string;
  userId?: number;
  quantity: number;
  unitPrice?: number;
  totalAmount?: number;
  createdAt?: string;
}

export interface DashboardStatsDTO {
  totalProducts: number;
  totalInventoryValue: number;
  lowStockProducts: any[];
  criticalAlertsCount: number;
  totalRevenue: number;
  totalInvestment: number;
  netProfit: number;
}

export interface StockPredictionDTO {
  productId: number;
  productName: string;
  currentStock: number;
  averageDailySales: number;
  daysUntilOutOfStock: number;
  status: 'CRITICAL' | 'WARNING' | 'STABLE';
}
