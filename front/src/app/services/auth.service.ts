import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponseModel } from '../models/authresponse.model';
import { LoginModel } from '../models/login.model';
import { PasswordResetModel } from '../models/passwordreset.model';
import { RegistroModel } from '../models/registro.model';

/**
 * Peticiones de autenticación: inicio de sesión, registro, verificación y recuperación de la contraseña.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/auth';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Envía las credenciales.
   *
   * @param username Nombre de usuario
   * @param password Contraseña
   * @returns Respuesta con el token, el rol y el nombre
   */
  login(username: string, password: string): Observable<AuthResponseModel> {
    const datos: LoginModel = { username: username, password: password };
    return this.http.post<AuthResponseModel>(this.url + '/login', datos);
  }

  /**
   * Registra una cuenta.
   *
   * @param datos Datos del registro
   * @returns Mensaje del back
   */
  registrar(datos: RegistroModel): Observable<string> {
    return this.http.post(this.url + '/register', datos, { responseType: 'text' });
  }

  /**
   * Verifica el correo con el token.
   *
   * @param token Token del enlace
   * @returns Mensaje del back
   */
  verificar(token: string): Observable<string> {
    return this.http.get(this.url + '/verify', { params: { token: token }, responseType: 'text' });
  }

  /**
   * Pide un nuevo enlace de verificación.
   *
   * @param email Correo de la cuenta
   * @returns Mensaje del back
   */
  reenviarVerificacion(email: string): Observable<string> {
    const datos: PasswordResetModel = { email: email };
    return this.http.post(this.url + '/resend-verification', datos, { responseType: 'text' });
  }

  /**
   * Pide el enlace de recuperación.
   *
   * @param email Correo de la cuenta
   * @returns Mensaje del back
   */
  olvido(email: string): Observable<string> {
    const datos: PasswordResetModel = { email: email };
    return this.http.post(this.url + '/forgot', datos, { responseType: 'text' });
  }

  /**
   * Cambia la contraseña con el token de recuperación.
   *
   * @param token Token del enlace
   * @param nuevaPassword Contraseña nueva
   * @returns Mensaje del back
   */
  restablecer(token: string, nuevaPassword: string): Observable<string> {
    const datos: PasswordResetModel = { token: token, nuevaPassword: nuevaPassword };
    return this.http.put(this.url + '/reset', datos, { responseType: 'text' });
  }
}
