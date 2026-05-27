import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductDTO } from '../models/product.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private url = `${environment.apiBaseUrl}/api/products`;

getAllProducts(): Observable<ProductDTO[]> {
  return this.http.get<ProductDTO[]>(this.url);
}

save(producto: any): Observable<any> {
  const headers = new HttpHeaders({
    'Content-Type': 'application/json'
  });
  
  return this.http.post(this.url, producto, { headers });
}

  searchProducts(name: string): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(`${this.url}/search?name=${name}`);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  updateProduct(id: number, product: ProductDTO): Observable<ProductDTO> {
    return this.http.put<ProductDTO>(`${this.url}/${id}`, product);
  }
}