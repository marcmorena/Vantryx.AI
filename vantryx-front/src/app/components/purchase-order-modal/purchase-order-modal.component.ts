import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductDTO } from '../../models/product.model';
import { Supplier } from '../../models/supplier.model';
import { PurchaseOrderRequestDTO, PurchaseOrderResponseDTO } from '../../models/purchase-order.model';
import { PurchaseOrderService } from '../../services/purchase-order.service';
import { ProductService } from '../../services/product.service';
import { SupplierService } from '../../services/supplier.service';

interface ItemEnCurso {
  productId: number;
  productName: string;
  quantity: number;
  priceAtPurchase: number;
}

@Component({
  selector: 'app-purchase-order-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase-order-modal.component.html',
  styleUrls: ['./purchase-order-modal.component.css']
})
export class PurchaseOrderModalComponent implements OnInit {
  @Input() preselectedProduct: ProductDTO | null = null;
  @Output() orderCreated = new EventEmitter<PurchaseOrderResponseDTO>();
  @Output() closed = new EventEmitter<void>();

  suppliers: Supplier[] = [];
  products: ProductDTO[] = [];

  selectedSupplierId: number | null = null;
  items: ItemEnCurso[] = [];

  currentProductId: number | null = null;
  currentQuantity = 1;
  currentPrice = 0;

  enviando = false;

  constructor(
    private supplierService: SupplierService,
    private productService: ProductService,
    private poService: PurchaseOrderService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.preselectedProduct) {
      this.selectedSupplierId = this.preselectedProduct.supplierId;
      this.items = [{
        productId: this.preselectedProduct.id!,
        productName: this.preselectedProduct.name,
        quantity: this.preselectedProduct.minStock,
        priceAtPurchase: this.preselectedProduct.purchasePrice
      }];
    } else {
      this.supplierService.getAll().subscribe(data => {
        this.suppliers = data;
        this.cdr.detectChanges();
      });
      this.productService.getAllProducts().subscribe(data => {
        this.products = data;
        this.cdr.detectChanges();
      });
    }
  }

  get productosFiltrados(): ProductDTO[] {
    if (!this.selectedSupplierId) return [];
    return this.products.filter(p => p.supplierId === Number(this.selectedSupplierId));
  }

  get totalEstimado(): number {
    return this.items.reduce((acc, i) => acc + i.quantity * i.priceAtPurchase, 0);
  }

  onProductoSeleccionado() {
    const prod = this.products.find(p => p.id === Number(this.currentProductId));
    if (prod) {
      this.currentPrice = prod.purchasePrice;
      this.currentQuantity = prod.minStock;
    }
  }

  addItem() {
    const prod = this.products.find(p => p.id === Number(this.currentProductId));
    if (!prod || this.currentQuantity <= 0) return;
    this.items.push({
      productId: prod.id!,
      productName: prod.name,
      quantity: this.currentQuantity,
      priceAtPurchase: this.currentPrice
    });
    this.currentProductId = null;
    this.currentQuantity = 1;
    this.currentPrice = 0;
  }

  removeItem(index: number) {
    this.items.splice(index, 1);
  }

  onProveedorCambiado() {
    this.currentProductId = null;
    this.currentQuantity = 1;
    this.currentPrice = 0;
  }

  submit() {
    if (!this.selectedSupplierId || this.items.length === 0) return;
    this.enviando = true;
    const dto: PurchaseOrderRequestDTO = {
      supplierId: Number(this.selectedSupplierId),
      items: this.items.map(i => ({
        productId: i.productId,
        quantity: i.quantity,
        priceAtPurchase: i.priceAtPurchase
      }))
    };
    this.poService.createOrder(dto).subscribe({
      next: (order) => {
        this.enviando = false;
        this.cdr.detectChanges();
        this.orderCreated.emit(order);
      },
      error: () => {
        alert('Error al crear la orden de compra.');
        this.enviando = false;
        this.cdr.detectChanges();
      }
    });
  }

  close() {
    this.closed.emit();
  }
}
