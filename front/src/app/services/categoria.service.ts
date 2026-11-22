import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CategoriaModel } from '../models/categoria.model';

/**
 * Peticiones de categorías.
 */
@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/categoria';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Consulta todos los registros.
   *
   * @returns Registros, o null si no hay
   */
  getAll(): Observable<CategoriaModel[] | null> {
    return this.http.get<CategoriaModel[] | null>(this.url + '/getall');
  }

  /**
   * Crea un registro.
   *
   * @param datos Datos que se envían
   * @returns Mensaje del back
   */
  crear(datos: CategoriaModel): Observable<string> {
    return this.http.post(this.url + '/createjson', datos, { responseType: 'text' });
  }

  /**
   * Actualiza un registro.
   *
   * @param id Identificador
   * @param datos Datos que se envían
   * @returns Mensaje del back
   */
  actualizar(id: number, datos: CategoriaModel): Observable<string> {
    return this.http.put(this.url + '/updatejson/' + id, datos, { responseType: 'text' });
  }

  /**
   * Desactiva o borra un registro.
   *
   * @param id Identificador
   * @returns Mensaje del back
   */
  eliminar(id: number): Observable<string> {
    return this.http.delete(this.url + '/deletebyid/' + id, { responseType: 'text' });
  }
}
