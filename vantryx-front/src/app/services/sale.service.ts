import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SaleDTO, DashboardStatsDTO, StockPredictionDTO } from '../models/sale.model';

@Injectable({ providedIn: 'root' })
export class SaleService {
  private salesUrl  = 'http://localhost:8080/api/sales';
  private statsUrl  = 'http://localhost:8080/api/stats/summary';
  private aiUrl     = 'http://localhost:8080/api/ai/predict';

  constructor(private http: HttpClient) {}

  getSales(): Observable<SaleDTO[]> {
    return this.http.get<SaleDTO[]>(this.salesUrl);
  }

  createSale(dto: SaleDTO): Observable<SaleDTO> {
    return this.http.post<SaleDTO>(this.salesUrl, dto);
  }

  getStats(): Observable<DashboardStatsDTO> {
    return this.http.get<DashboardStatsDTO>(this.statsUrl);
  }

  getPrediction(productId: number): Observable<StockPredictionDTO> {
    return this.http.get<StockPredictionDTO>(`${this.aiUrl}/${productId}`);
  }
}
