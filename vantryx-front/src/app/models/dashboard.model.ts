export interface DashboardDTO {
  totalProducts: number;
  totalInventoryValue: number;
  lowStockProducts: any[]; // Luego crearemos el ProductDTO detallado
  criticalAlertsCount: number;
  totalRevenue: number;
  totalInvestment: number;
  netProfit: number;
}

export interface ProductDTO {
  id: number;
  name: string;
  currentStock: number;
  minStock: number;
  categoryName: string;
}