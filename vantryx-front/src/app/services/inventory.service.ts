import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MovementResponseDTO, StockMovementRequestDTO } from '../models/movement.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private baseUrl = `${environment.apiBaseUrl}/api/inventory`;

  constructor(private http: HttpClient) { }

  registrarMovimiento(movimiento: StockMovementRequestDTO): Observable<string> {
    return this.http.post(`${this.baseUrl}/movement`, movimiento, { responseType: 'text' });
  }

  getProductHistory(productId: number): Observable<MovementResponseDTO[]> {
    return this.http.get<MovementResponseDTO[]>(`${this.baseUrl}/product/${productId}/history`);
  }
}