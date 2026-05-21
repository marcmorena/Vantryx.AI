export interface ProductDTO {
  id?: number;
  name: string;
  sku: string;
  description?: string;
  salePrice: number;
  purchasePrice: number;
  leadTime: number;
  currentStock: number;
  minStock: number;
  categoryId: number;
  categoryName?: string; // Viene en el GET, pero no es obligatorio para el POST
  supplierId: number;
  supplierName?: string; // Viene en el GET
}