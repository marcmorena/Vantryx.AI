import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { CategoryDTO } from '../models/category.model';
import { Subject } from 'rxjs'; // Añade esta importación

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/api/categories';
  // Alarma para avisar que hay categorías nuevas
  private _refreshNeeded$ = new Subject<void>();

  // ==========================================
  // ESTADO GLOBAL: Filtro de Categoría
  // ==========================================
  // Guarda el ID seleccionado. Empieza en null (mostrar todo)
  private categoriaSeleccionadaSource = new BehaviorSubject<number | null>(null);
  
  // Observable que escucharán otros componentes
  categoriaSeleccionada$ = this.categoriaSeleccionadaSource.asObservable();

  // Método para actualizar el filtro desde el Dashboard
  setCategoriaSeleccionada(id: number | null) {
    this.categoriaSeleccionadaSource.next(id);
  }
  // ==========================================

  getAll(): Observable<CategoryDTO[]> {
    return this.http.get<CategoryDTO[]>(this.url);
  }

  save(category: any): Observable<any> {
    return this.http.post<any>(this.url, category).pipe(
      tap(() => {
        this._refreshNeeded$.next(); // Toca la alarma
      })
    );
  }

  // category.service.ts

deleteAndMove(id: number, targetId: number): Observable<any> {
  // Llamada al endpoint: DELETE /api/categories/{id}/move-to/{newId}
  return this.http.delete(`${this.url}/${id}/move-to/${targetId}`).pipe(
    tap(() => {
      // Avisamos a toda la app que la lista de categorías ha cambiado
      this.refreshNeeded$.next();
    })
  );
}

  get refreshNeeded$() {
    return this._refreshNeeded$;
  }
}