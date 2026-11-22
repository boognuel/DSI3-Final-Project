import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { LugarModel } from '../models/lugar.model';

/**
 * Peticiones de lugares.
 */
@Injectable({
  providedIn: 'root',
})
export class LugarService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/lugar';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Consulta los lugares, con filtros opcionales.
   *
   * @param zonaId Zona, o null para todas
   * @param categoriaId Categoría, o null para todas
   * @returns Lugares del más económico al más costoso
   */
  getAll(zonaId: number | null, categoriaId: number | null): Observable<LugarModel[]> {
    let params: HttpParams = new HttpParams();
    if (zonaId !== null) {
      params = params.set('zonaId', String(zonaId));
    }
    if (categoriaId !== null) {
      params = params.set('categoriaId', String(categoriaId));
    }
    return this.http.get<LugarModel[]>(this.url + '/getall', { params: params });
  }

  /**
   * Crea un lugar.
   *
   * @param datos Datos del lugar
   * @returns Lugar creado
   */
  crear(datos: LugarModel): Observable<LugarModel> {
    return this.http.post<LugarModel>(this.url + '/createjson', datos);
  }

  /**
   * Actualiza un lugar.
   *
   * @param id Identificador
   * @param datos Datos del lugar
   * @returns Mensaje del back
   */
  actualizar(id: number, datos: LugarModel): Observable<string> {
    return this.http.put(this.url + '/updatejson/' + id, datos, { responseType: 'text' });
  }

  /**
   * Borra un lugar.
   *
   * @param id Identificador
   * @returns Mensaje del back
   */
  eliminar(id: number): Observable<string> {
    return this.http.delete(this.url + '/deletebyid/' + id, { responseType: 'text' });
  }

  /**
   * Carga o actualiza los lugares desde OpenStreetMap.
   *
   * @returns Resumen del back
   */
  cargarOsm(): Observable<string> {
    return this.http.post(this.url + '/cargarosm', null, { responseType: 'text' });
  }
}
