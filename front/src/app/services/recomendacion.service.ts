import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RecomendacionModel } from '../models/recomendacion.model';

/**
 * Peticiones de la búsqueda de planes.
 */
@Injectable({
  providedIn: 'root',
})
export class RecomendacionService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/recomendacion';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Busca los planes que caben en el presupuesto.
   *
   * @param presupuesto Dinero por persona
   * @param categoriaIds Categorías elegidas; vacía para todas
   * @returns Zonas y lugares recomendados
   */
  buscar(presupuesto: number, categoriaIds: number[]): Observable<RecomendacionModel> {
    let params: HttpParams = new HttpParams().set('presupuesto', String(presupuesto));
    for (const id of categoriaIds) {
      params = params.append('categoriaIds', String(id));
    }
    return this.http.get<RecomendacionModel>(this.url, { params: params });
  }
}
