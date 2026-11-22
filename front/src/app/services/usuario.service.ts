import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UsuarioModel } from '../models/usuario.model';

/**
 * Peticiones de administración de usuarios.
 */
@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/user';

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
  getAll(): Observable<UsuarioModel[] | null> {
    return this.http.get<UsuarioModel[] | null>(this.url + '/getall');
  }

  /**
   * Crea un registro.
   *
   * @param datos Datos del usuario
   * @returns Mensaje del back
   */
  crear(datos: UsuarioModel): Observable<string> {
    return this.http.post(this.url + '/createjson', datos, { responseType: 'text' });
  }

  /**
   * Actualiza un registro.
   *
   * @param id Identificador
   * @param datos Datos del usuario
   * @returns Mensaje del back
   */
  actualizar(id: number, datos: UsuarioModel): Observable<string> {
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
