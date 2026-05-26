import { Component, Input, Output, EventEmitter, OnDestroy, ChangeDetectorRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiAdvisorService } from '../../services/ai-advisor.service';
import { ProductDTO } from '../../models/product.model';

interface AnalisisHistorial {
  productoNombre: string;
  productoId: number;
  respuesta: string;
  timestamp: Date;
  duracionSegundos: number;
}

@Component({
  selector: 'app-asistente-ia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './asistente-ia.component.html',
  styleUrls: ['./asistente-ia.component.css']
})
export class AsistenteIaComponent implements OnDestroy {
  @Input() productos: ProductDTO[] = [];
  @Output() cerrar = new EventEmitter<void>();

  // --- Estado del formulario ---
  productoSeleccionadoId: number | null = null;

  // --- Estado de la consulta ---
  analizando = false;
  respuestaActual: string | null = null;
  error: string | null = null;

  // --- Timer de espera ---
  segundosTranscurridos = 0;
  private timerInterval: any = null;

  // --- Historial de sesión ---
  historial: AnalisisHistorial[] = [];
  mostrarHistorial = false;

  constructor(
    private aiService: AiAdvisorService,
    private cdr: ChangeDetectorRef
  ) {}

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (!this.analizando) this.cerrar.emit();
  }

  ngOnDestroy(): void {
    this.detenerTimer();
  }

  get productoActual(): ProductDTO | undefined {
    return this.productos.find(p => p.id === Number(this.productoSeleccionadoId));
  }

  get margenBruto(): string {
    const p = this.productoActual;
    if (!p || !p.purchasePrice || !p.salePrice) return '—';
    const margen = ((p.salePrice - p.purchasePrice) / p.salePrice) * 100;
    return margen.toFixed(1) + '%';
  }

  get timerLabel(): string {
    if (this.segundosTranscurridos < 60) {
      return `${this.segundosTranscurridos}s`;
    }
    const m = Math.floor(this.segundosTranscurridos / 60);
    const s = this.segundosTranscurridos % 60;
    return `${m}m ${s}s`;
  }

  get respuestaFormateada(): string {
    if (!this.respuestaActual) return '';
    return this.respuestaActual
      .replace(/\n\n/g, '</p><p class="mb-3">')
      .replace(/\n/g, '<br>');
  }

  consultar(): void {
    if (!this.productoSeleccionadoId || this.analizando) return;

    this.analizando = true;
    this.respuestaActual = null;
    this.error = null;
    this.segundosTranscurridos = 0;
    this.iniciarTimer();
    this.cdr.detectChanges();

    const inicio = Date.now();

    this.aiService.analyzeProduct(Number(this.productoSeleccionadoId)).subscribe({
      next: (respuesta: string) => {
        const duracion = Math.round((Date.now() - inicio) / 1000);
        this.detenerTimer();
        this.respuestaActual = respuesta;
        this.analizando = false;

        if (this.productoActual) {
          this.historial.unshift({
            productoNombre: this.productoActual.name,
            productoId: Number(this.productoSeleccionadoId),
            respuesta,
            timestamp: new Date(),
            duracionSegundos: duracion
          });
        }

        this.cdr.detectChanges();
      },
      error: (err) => {
        this.detenerTimer();
        this.analizando = false;

        if (err.status === 0) {
          this.error = 'No se puede conectar con el servidor. Verifica que el backend esté activo.';
        } else if (err.status === 500) {
          this.error = 'El modelo de IA no está disponible. Verifica que Ollama esté ejecutándose con el modelo phi3.';
        } else {
          this.error = `Error inesperado (${err.status}). Inténtalo de nuevo.`;
        }

        this.cdr.detectChanges();
      }
    });
  }

  cargarDesdeHistorial(item: AnalisisHistorial): void {
    this.productoSeleccionadoId = item.productoId;
    this.respuestaActual = item.respuesta;
    this.error = null;
    this.mostrarHistorial = false;
    this.cdr.detectChanges();
  }

  limpiar(): void {
    this.respuestaActual = null;
    this.error = null;
    this.productoSeleccionadoId = null;
    this.segundosTranscurridos = 0;
    this.cdr.detectChanges();
  }

  private iniciarTimer(): void {
    this.timerInterval = setInterval(() => {
      this.segundosTranscurridos++;
      this.cdr.detectChanges();
    }, 1000);
  }

  private detenerTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }
}
