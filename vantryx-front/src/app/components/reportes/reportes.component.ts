import { Component, Input, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportService } from '../../services/report.service';
import { SaleService } from '../../services/sale.service';
import { DashboardStatsDTO } from '../../models/sale.model';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reportes.component.html',
  styleUrls: ['./reportes.component.css']
})
export class ReportesComponent {
  // Recibimos los stats del padre para mostrar la preview sin petición extra
  @Input() set stats(value: DashboardStatsDTO | null) {
    this._stats = value;
    this.cdr.detectChanges();
  }
  get stats(): DashboardStatsDTO | null { return this._stats; }
  private _stats: DashboardStatsDTO | null = null;

  descargando = false;
  descargaExitosa = false;
  errorDescarga = false;

  constructor(
    private reportService: ReportService,
    private cdr: ChangeDetectorRef
  ) {}

  descargarReporte(): void {
    this.descargando = true;
    this.descargaExitosa = false;
    this.errorDescarga = false;
    this.cdr.detectChanges();

    this.reportService.downloadInventoryReport().subscribe({
      next: (blob: Blob) => {
        // Creamos un enlace temporal para forzar la descarga en el navegador
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        const fecha = new Date().toISOString().slice(0, 10); // YYYY-MM-DD
        anchor.href = url;
        anchor.download = `Reporte_Stock_Critico_Vantryx_${fecha}.xlsx`;
        anchor.click();
        window.URL.revokeObjectURL(url);

        this.descargando = false;
        this.descargaExitosa = true;
        this.cdr.detectChanges();

        // Ocultamos el mensaje de éxito tras 5 segundos
        setTimeout(() => {
          this.descargaExitosa = false;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: () => {
        this.descargando = false;
        this.errorDescarga = true;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.errorDescarga = false;
          this.cdr.detectChanges();
        }, 5000);
      }
    });
  }

  // Helpers de presentación
  getStockStatusClass(prod: any): string {
    if (prod.currentStock === 0) return 'text-red-400';
    if (prod.currentStock <= prod.minStock) return 'text-yellow-400';
    return 'text-emerald-400';
  }

  getStockStatusIcon(prod: any): string {
    if (prod.currentStock === 0) return '🔴';
    if (prod.currentStock <= prod.minStock) return '🟡';
    return '🟢';
  }
}
