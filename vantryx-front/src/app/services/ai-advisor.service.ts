import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AiAdvisorService {
  private baseUrl = 'http://localhost:8080/api/v1/ai';

  constructor(private http: HttpClient) {}

  // El backend devuelve texto plano generado por Ollama (phi3)
  // Puede tardar 15-60s dependiendo del hardware — timeout del backend: 5 min
  analyzeProduct(productId: number): Observable<string> {
    return this.http.get(`${this.baseUrl}/analyze/${productId}`, {
      responseType: 'text'
    });
  }
}
