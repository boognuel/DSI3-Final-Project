import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CambioPasswordModel } from '../models/cambiopassword.model';
import { UsuarioModel } from '../models/usuario.model';

/**
 * Peticiones del perfil del usuario con sesión.
 */
@Injectable({
  providedIn: 'root',
})
export class PerfilService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/perfil';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Consulta los datos propios.
   *
   * @returns Datos del usuario
   */
  get(): Observable<UsuarioModel> {
    return this.http.get<UsuarioModel>(this.url + '/get');
  }

  /**
   * Cambia el nombre de usuario y el correo.
   *
   * @param username Nombre de usuario
   * @param email Correo
   * @returns Mensaje del back
   */
  actualizar(username: string, email: string): Observable<string> {
    return this.http.put(
      this.url + '/updatejson',
      { username: username, email: email },
      { responseType: 'text' },
    );
  }

  /**
   * Cambia la contraseña.
   *
   * @param datos Contraseña actual y nueva
   * @returns Mensaje del back
   */
  cambiarPassword(datos: CambioPasswordModel): Observable<string> {
    return this.http.put(this.url + '/password', datos, { responseType: 'text' });
  }
}
