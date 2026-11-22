import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { InvitacionModel } from '../models/invitacion.model';

/**
 * Peticiones de invitaciones a salidas.
 */
@Injectable({
  providedIn: 'root',
})
export class InvitacionService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/invitacion';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Envía una invitación por correo.
   *
   * @param salidaId Salida
   * @param email Correo del invitado
   * @returns Mensaje del back
   */
  enviar(salidaId: number, email: string): Observable<string> {
    return this.http.post(
      this.url + '/enviar',
      { salidaId: salidaId, emailInvitado: email },
      { responseType: 'text' },
    );
  }

  /**
   * Lista las invitaciones de una salida.
   *
   * @param salidaId Salida
   * @returns Invitaciones
   */
  porSalida(salidaId: number): Observable<InvitacionModel[]> {
    return this.http.get<InvitacionModel[]>(this.url + '/salida/' + salidaId);
  }

  /**
   * Lista las invitaciones que recibió el usuario.
   *
   * @returns Invitaciones
   */
  mias(): Observable<InvitacionModel[] | null> {
    return this.http.get<InvitacionModel[] | null>(this.url + '/mis-invitaciones');
  }

  /**
   * Acepta o rechaza una invitación con el token del correo.
   *
   * @param token Token de la invitación
   * @param aceptar true para aceptar
   * @returns Mensaje del back
   */
  responder(token: string, aceptar: boolean): Observable<string> {
    return this.http.post(this.url + '/responder', null, {
      params: { token: token, aceptar: String(aceptar) },
      responseType: 'text',
    });
  }
}
