import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SaleService } from '../../services/sale.service';
import { SaleDTO, DashboardStatsDTO, StockPredictionDTO } from '../../models/sale.model';
import { ProductDTO } from '../../models/product.model';

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './ventas.component.html',
  styleUrls: ['./ventas.component.css']
})
export class VentasComponent implements OnInit {
  @Input() productos: ProductDTO[] = [];

  // --- Estadísticas financieras ---
  stats: DashboardStatsDTO | null = null;
  cargandoStats = false;

  // --- Historial de ventas ---
  ventas: SaleDTO[] = [];
  cargandoVentas = false;

  // --- Formulario de venta ---
  productoSeleccionadoId: number | null = null;
  cantidad = 1;
  enviando = false;

  // --- Predicción IA ---
  prediccion: StockPredictionDTO | null = null;
  cargandoPrediccion = false;

  constructor(
    private saleService: SaleService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarStats();
    this.cargarHistorial();
  }

  // ── Datos ──────────────────────────────────────────────

  cargarStats(): void {
    this.cargandoStats = true;
    this.saleService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.cargandoStats = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando estadísticas:', err);
        this.cargandoStats = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarHistorial(): void {
    this.cargandoVentas = true;
    this.saleService.getSales().subscribe({
      next: (data) => {
        this.ventas = data;
        this.cargandoVentas = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando historial de ventas:', err);
        this.cargandoVentas = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Selector de producto ───────────────────────────────

  get productoActual(): ProductDTO | undefined {
    return this.productos.find(p => p.id === Number(this.productoSeleccionadoId));
  }

  get totalVenta(): number {
    return (this.productoActual?.salePrice ?? 0) * this.cantidad;
  }

  get stockInsuficiente(): boolean {
    return !!this.productoActual && this.cantidad > this.productoActual.currentStock;
  }

  onProductoChange(): void {
    this.cantidad = 1;
    this.prediccion = null;

    if (this.productoSeleccionadoId) {
      this.cargarPrediccion(Number(this.productoSeleccionadoId));
    }
    this.cdr.detectChanges();
  }

  cargarPrediccion(productId: number): void {
    this.cargandoPrediccion = true;
    this.saleService.getPrediction(productId).subscribe({
      next: (data) => {
        this.prediccion = data;
        this.cargandoPrediccion = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.prediccion = null;
        this.cargandoPrediccion = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Registro de venta ──────────────────────────────────

  registrarVenta(): void {
    if (!this.productoSeleccionadoId || this.cantidad <= 0 || this.stockInsuficiente) return;

    this.enviando = true;
    const dto: SaleDTO = {
      productId: Number(this.productoSeleccionadoId),
      quantity: this.cantidad
    };

    this.saleService.createSale(dto).subscribe({
      next: () => {
        this.enviando = false;
        this.productoSeleccionadoId = null;
        this.cantidad = 1;
        this.prediccion = null;
        // Refrescamos historial y stats
        this.cargarHistorial();
        this.cargarStats();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error registrando venta:', err);
        const msg = err?.error?.message ?? 'Error al registrar la venta. Verifica el stock disponible.';
        alert(msg);
        this.enviando = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ── Helpers de presentación ────────────────────────────

  getPredictionBadgeClass(): string {
    switch (this.prediccion?.status) {
      case 'CRITICAL': return 'bg-red-500/20 text-red-400 border border-red-500/30';
      case 'WARNING':  return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30';
      default:         return 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30';
    }
  }

  getPredictionIcon(): string {
    switch (this.prediccion?.status) {
      case 'CRITICAL': return '🚨';
      case 'WARNING':  return '⚠️';
      default:         return '✅';
    }
  }

  getPredictionLabel(): string {
    if (!this.prediccion) return '';
    const { averageDailySales, daysUntilOutOfStock, status } = this.prediccion;
    const media = averageDailySales.toFixed(1);
    if (status === 'STABLE' && daysUntilOutOfStock >= 999) {
      return `~${media} ud/día · Stock suficiente`;
    }
    return `~${media} ud/día · Sin stock en ${daysUntilOutOfStock} días`;
  }

  getNetProfitClass(): string {
    if (!this.stats) return 'text-slate-400';
    return this.stats.netProfit >= 0 ? 'text-emerald-400' : 'text-red-400';
  }
}
