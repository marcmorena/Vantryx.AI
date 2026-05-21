import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardDTO } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class StatsService {

  // Gracias al proxy.conf.json, esta ruta apuntará a http://localhost:8080/api/stats/summary
  private apiUrl = 'http://localhost:8080/api/stats/summary';

  constructor(private http: HttpClient) { }

  getSummary(): Observable<DashboardDTO> {
    return this.http.get<DashboardDTO>(this.apiUrl);
  }
}
