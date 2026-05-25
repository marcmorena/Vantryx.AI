import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private baseUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  // Descarga el Excel Y envía el email automáticamente (comportamiento del backend)
  downloadInventoryReport(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/inventory`, { responseType: 'blob' });
  }
}
