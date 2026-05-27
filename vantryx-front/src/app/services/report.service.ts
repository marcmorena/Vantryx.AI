import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private baseUrl = `${environment.apiBaseUrl}/api/reports`;

  constructor(private http: HttpClient) {}

  // Descarga el Excel Y envía el email automáticamente (comportamiento del backend)
  downloadInventoryReport(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/inventory`, { responseType: 'blob' });
  }
}
