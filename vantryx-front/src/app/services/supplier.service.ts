import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Supplier } from '../models/supplier.model';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/api/v1/suppliers';

  getAll(): Observable<Supplier[]> {
    return this.http.get<Supplier[]>(this.url); 
  }

  // Guardar nuevo proveedor
  save(supplier: any): Observable<any> {
    return this.http.post<any>(this.url, supplier);
  }

  update(id: number, supplier: any): Observable<any> {
  return this.http.put(`${this.url}/${id}`, supplier);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.url}/${id}`, { responseType: 'text' });
  }
}