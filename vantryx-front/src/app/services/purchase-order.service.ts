import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PurchaseOrderResponseDTO, PurchaseOrderRequestDTO } from '../models/purchase-order.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PurchaseOrderService {
  private apiUrl = `${environment.apiBaseUrl}/api/purchase-orders`;

  constructor(private http: HttpClient) { }

  // Obtener órdenes DRAFT y ORDERED
  getPendingOrders(): Observable<PurchaseOrderResponseDTO[]> {
    return this.http.get<PurchaseOrderResponseDTO[]>(`${this.apiUrl}/pending`);
  }

  // Crear una orden manualmente
  createOrder(dto: PurchaseOrderRequestDTO): Observable<PurchaseOrderResponseDTO> {
    return this.http.post<PurchaseOrderResponseDTO>(this.apiUrl, dto);
  }

  // Recibir orden (Aumenta stock)
  receiveOrder(id: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/${id}/receive`, {}, { responseType: 'text' });
  }

  // ¡La joya de la corona! Generar sugerencias basadas en stock bajo
  generateSuggestions(): Observable<PurchaseOrderResponseDTO[]> {
    return this.http.post<PurchaseOrderResponseDTO[]>(`${this.apiUrl}/generate-suggestions`, {});
  }

  confirmOrder(id: number): Observable<PurchaseOrderResponseDTO> {
  return this.http.put<PurchaseOrderResponseDTO>(`${this.apiUrl}/${id}/confirm`, {});
}
}