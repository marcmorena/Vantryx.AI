import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PurchaseOrderService } from '../../services/purchase-order.service';
import { PurchaseOrderResponseDTO, OrderStatus } from '../../models/purchase-order.model';
import { PurchaseOrderModalComponent } from '../purchase-order-modal/purchase-order-modal.component';

@Component({
  selector: 'app-compras',
  standalone: true,
  templateUrl: './compras.component.html',
  imports: [CommonModule, PurchaseOrderModalComponent],
  styleUrls: ['./compras.component.css']
})
export class ComprasComponent implements OnInit {
  @Output() stockActualizado = new EventEmitter<void>();

  ordenes: PurchaseOrderResponseDTO[] = [];
  cargando = false;
  modalAbierto = false;

  constructor(
    private poService: PurchaseOrderService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.listarPendientes();
  }

  listarPendientes() {
    this.cargando = true;
    this.poService.getPendingOrders().subscribe({
      next: (data) => {
        this.ordenes = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  generarSugerencias() {
    this.cargando = true;
    this.poService.generateSuggestions().subscribe({
      next: (nuevas) => {
        alert(`${nuevas.length} sugerencias de compra generadas en estado BORRADOR.`);
        this.listarPendientes();
      },
      error: (err) => {
        this.cargando = false;
        this.cdr.detectChanges();
        alert('No hay alertas de stock para generar sugerencias.');
      }
    });
  }

  confirmarRecepcion(order: PurchaseOrderResponseDTO) {
    if (confirm(`¿Confirmas que has recibido los productos de la Orden #${order.id}? El stock se actualizará automáticamente.`)) {
      this.poService.receiveOrder(order.id).subscribe({
        next: (msg) => {
          alert(msg);
          this.listarPendientes();
          this.stockActualizado.emit(); // Notifica al AppComponent para refrescar el dashboard
        },
        error: (err) => {
          this.cdr.detectChanges();
          alert('Error al recibir la orden.');
        }
      });
    }
  }

  ordenesPorEstado(estado: string): PurchaseOrderResponseDTO[] {
    return this.ordenes.filter(o => o.status === estado);
  }

  abrirModal() {
    this.modalAbierto = true;
    this.cdr.detectChanges();
  }

  cerrarModal() {
    this.modalAbierto = false;
    this.cdr.detectChanges();
  }

  onOrdenCreada(order: PurchaseOrderResponseDTO) {
    this.modalAbierto = false;
    this.listarPendientes();
  }

  confirmarPedido(order: PurchaseOrderResponseDTO) {
    this.poService.confirmOrder(order.id).subscribe({
      next: (actualizada) => {
        this.listarPendientes();
        console.log('Pedido confirmado:', actualizada.id);
      },
      error: (err) => {
        this.cdr.detectChanges();
        alert('Error al confirmar el pedido');
      }
    });
  }
}