import { Component, Input, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../services/inventory.service';
import { MovementResponseDTO, MovementType, StockMovementRequestDTO } from '../../models/movement.model';
import { ProductDTO } from '../../models/product.model';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './movimientos.component.html',
  styleUrls: ['./movimientos.component.css']
})
export class MovimientosComponent implements OnChanges {
  @Input() productos: ProductDTO[] = [];

  // --- Estado de selección ---
  productoSeleccionadoId: number | null = null;
  historial: MovementResponseDTO[] = [];
  cargandoHistorial = false;

  // --- Filtro de tipo ---
  filtroTipo: string = 'TODOS';

  // --- Formulario de nuevo movimiento ---
  mostrarFormMovimiento = false;
  nuevoMovimiento: StockMovementRequestDTO = {
    productId: 0,
    quantity: 1,
    type: 'OUT',
    reason: ''
  };
  motivosPreset: Record<MovementType, string[]> = {
    OUT: ['Venta en tienda', 'Venta online', 'Muestra/demostración', 'Pérdida o rotura', 'Caducidad'],
    IN:  ['Compra a proveedor', 'Devolución de cliente', 'Transferencia entre almacenes'],
    ADJUSTMENT: ['Corrección de inventario', 'Recuento físico', 'Error de sistema']
  };
  enviando = false;

  constructor(
    private inventoryService: InventoryService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    // Si la lista de productos llega después (carga asíncrona), y ya hay uno seleccionado, recargamos
    if (changes['productos'] && this.productoSeleccionadoId) {
      this.cdr.detectChanges();
    }
  }

  get productoActual(): ProductDTO | undefined {
    return this.productos.find(p => p.id === Number(this.productoSeleccionadoId));
  }

  get historialFiltrado(): MovementResponseDTO[] {
    if (this.filtroTipo === 'TODOS') return this.historial;
    return this.historial.filter(m => m.type === this.filtroTipo);
  }

  get motivosDisponibles(): string[] {
    return this.motivosPreset[this.nuevoMovimiento.type] ?? [];
  }

  // --- Velocidad de ventas (IA predictiva básica) ---
  get estadisticas(): { velocidadSemanal: number; diasHastaAgotamiento: number | null; totalSalidas30d: number } {
    const hace30dias = new Date();
    hace30dias.setDate(hace30dias.getDate() - 30);

    const salidas30d = this.historial.filter(m =>
      m.type === 'OUT' && new Date(m.createdAt) >= hace30dias
    );
    const totalSalidas30d = salidas30d.reduce((acc, m) => acc + m.quantity, 0);
    const velocidadSemanal = parseFloat((totalSalidas30d / 4.33).toFixed(1)); // 30 días ≈ 4.33 semanas

    let diasHastaAgotamiento: number | null = null;
    const producto = this.productoActual;
    if (producto && velocidadSemanal > 0) {
      const velocidadDiaria = velocidadSemanal / 7;
      diasHastaAgotamiento = Math.floor(producto.currentStock / velocidadDiaria);
    }

    return { velocidadSemanal, diasHastaAgotamiento, totalSalidas30d };
  }

  setFiltro(tipo: string): void {
    this.filtroTipo = tipo;
    this.cdr.detectChanges();
  }

  // Necesario para que el template pueda asignar string al tipo MovementType de forma segura
  setTipoMovimiento(tipo: string): void {
    this.nuevoMovimiento.type = tipo as MovementType;
    this.onTipoMovimientoChange();
  }

  onProductoChange(): void {
    this.historial = [];
    this.mostrarFormMovimiento = false;
    this.filtroTipo = 'TODOS';
    if (this.productoSeleccionadoId) {
      this.nuevoMovimiento.productId = Number(this.productoSeleccionadoId);
      this.cargarHistorial();
    }
    this.cdr.detectChanges();
  }

  cargarHistorial(): void {
    if (!this.productoSeleccionadoId) return;
    this.cargandoHistorial = true;
    this.inventoryService.getProductHistory(Number(this.productoSeleccionadoId)).subscribe({
      next: (data) => {
        this.historial = data;
        this.cargandoHistorial = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando historial:', err);
        this.cargandoHistorial = false;
        this.cdr.detectChanges();
      }
    });
  }

  abrirFormMovimiento(): void {
    this.nuevoMovimiento = {
      productId: Number(this.productoSeleccionadoId),
      quantity: 1,
      type: 'OUT',
      reason: this.motivosPreset['OUT'][0]
    };
    this.mostrarFormMovimiento = true;
    this.cdr.detectChanges();
  }

  onTipoMovimientoChange(): void {
    this.nuevoMovimiento.reason = this.motivosDisponibles[0] ?? '';
    this.cdr.detectChanges();
  }

  guardarMovimiento(): void {
    if (!this.nuevoMovimiento.productId || this.nuevoMovimiento.quantity <= 0 || !this.nuevoMovimiento.reason.trim()) {
      return;
    }
    this.enviando = true;
    this.inventoryService.registrarMovimiento(this.nuevoMovimiento).subscribe({
      next: () => {
        this.enviando = false;
        this.mostrarFormMovimiento = false;
        this.cargarHistorial(); // Recargamos el historial para reflejar el nuevo movimiento
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error registrando movimiento:', err);
        alert('Error al registrar el movimiento. Comprueba que el stock no quede negativo.');
        this.enviando = false;
        this.cdr.detectChanges();
      }
    });
  }

  // --- Helpers de presentación ---
  getBadgeClass(type: MovementType): string {
    switch (type) {
      case 'IN':         return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30';
      case 'OUT':        return 'bg-red-500/20 text-red-400 border-red-500/30';
      case 'ADJUSTMENT': return 'bg-blue-500/20 text-blue-400 border-blue-500/30';
    }
  }

  getBadgeLabel(type: MovementType): string {
    switch (type) {
      case 'IN':         return 'ENTRADA';
      case 'OUT':        return 'SALIDA';
      case 'ADJUSTMENT': return 'AJUSTE';
    }
  }

  getQuantityDisplay(m: MovementResponseDTO): string {
    switch (m.type) {
      case 'IN':         return `+${m.quantity}`;
      case 'OUT':        return `-${m.quantity}`;
      case 'ADJUSTMENT': return `=${m.quantity}`;
    }
  }

  getQuantityClass(type: MovementType): string {
    switch (type) {
      case 'IN':         return 'text-emerald-400';
      case 'OUT':        return 'text-red-400';
      case 'ADJUSTMENT': return 'text-blue-400';
    }
  }

  getStockAlertClass(): string {
    const dias = this.estadisticas.diasHastaAgotamiento;
    if (dias === null) return '';
    if (dias <= 7)  return 'text-red-400';
    if (dias <= 21) return 'text-yellow-400';
    return 'text-emerald-400';
  }
}
