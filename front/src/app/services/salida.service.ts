import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SalidaModel } from '../models/salida.model';
import { SalidaLugarModel } from '../models/salidalugar.model';

/**
 * Peticiones de salidas.
 */
@Injectable({
  providedIn: 'root',
})
export class SalidaService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/salida';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Crea una salida.
   *
   * @param salida Datos de la salida
   * @returns Salida creada
   */
  crear(salida: SalidaModel): Observable<SalidaModel> {
    return this.http.post<SalidaModel>(this.url + '/createjson', salida);
  }

  /**
   * Lista las salidas que organiza el usuario y las que aceptó.
   *
   * @returns Salidas
   */
  mias(): Observable<SalidaModel[] | null> {
    return this.http.get<SalidaModel[] | null>(this.url + '/mis-salidas');
  }

  /**
   * Lista las salidas de todos los usuarios. Solo para el administrador.
   *
   * @returns Salidas
   */
  getAll(): Observable<SalidaModel[]> {
    return this.http.get<SalidaModel[]>(this.url + '/getall');
  }

  /**
   * Consulta una salida.
   *
   * @param id Identificador
   * @returns Salida
   */
  getById(id: number): Observable<SalidaModel> {
    return this.http.get<SalidaModel>(this.url + '/getbyid/' + id);
  }

  /**
   * Actualiza una salida. Cambiar el estado a CANCELADA la cancela.
   *
   * @param id Identificador
   * @param salida Datos de la salida
   * @returns Mensaje del back
   */
  actualizar(id: number, salida: SalidaModel): Observable<string> {
    return this.http.put(this.url + '/updatejson/' + id, salida, { responseType: 'text' });
  }

  /**
   * Borra una salida.
   *
   * @param id Identificador
   * @returns Mensaje del back
   */
  eliminar(id: number): Observable<string> {
    return this.http.delete(this.url + '/deletebyid/' + id, { responseType: 'text' });
  }

  /**
   * Propone un lugar en la salida.
   *
   * @param id Salida
   * @param lugarId Lugar
   * @returns Mensaje del back
   */
  agregarLugar(id: number, lugarId: number): Observable<string> {
    return this.http.post(
      this.url + '/' + id + '/lugares',
      { lugarId: lugarId },
      { responseType: 'text' },
    );
  }

  /**
   * Lista los lugares propuestos en la salida.
   *
   * @param id Salida
   * @returns Lugares propuestos
   */
  lugares(id: number): Observable<SalidaLugarModel[]> {
    return this.http.get<SalidaLugarModel[]>(this.url + '/' + id + '/lugares');
  }

  /**
   * Elige el destino de la salida.
   *
   * @param id Salida
   * @param lugarId Lugar elegido
   * @returns Mensaje del back
   */
  seleccionar(id: number, lugarId: number): Observable<string> {
    return this.http.put(this.url + '/' + id + '/lugares/' + lugarId + '/seleccionar', null, {
      responseType: 'text',
    });
  }
}
