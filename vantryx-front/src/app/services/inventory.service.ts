import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MovementResponseDTO, StockMovementRequestDTO } from '../models/movement.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private baseUrl = 'http://localhost:8080/api/inventory';

  constructor(private http: HttpClient) { }

  registrarMovimiento(movimiento: StockMovementRequestDTO): Observable<string> {
    return this.http.post(`${this.baseUrl}/movement`, movimiento, { responseType: 'text' });
  }

  getProductHistory(productId: number): Observable<MovementResponseDTO[]> {
    return this.http.get<MovementResponseDTO[]>(`${this.baseUrl}/product/${productId}/history`);
  }
}