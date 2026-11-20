import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponseModel } from '../models/auth-response.model';
import { LoginModel } from '../models/login.model';
import { PasswordResetModel } from '../models/password-reset.model';
import { RegistroModel } from '../models/registro.model';
import { SesionModel } from '../models/sesion.model';

const CLAVE = 'bogoparche.sesion';

@Injectable({
  providedIn: 'root',
})
export class Authservice {
  // Cliente que se comunica con el back
  private cliente = inject(HttpClient);
  private readonly urlbase: string = `${environment.apiUrl}/auth`;

  private sesionActual = signal<SesionModel | null>(this.leerSesion());

  readonly sesion = this.sesionActual.asReadonly();
  readonly logueado = computed(() => this.sesionActual() !== null);
  readonly esAdmin = computed(() => this.sesionActual()?.role === 'ADMIN');
  readonly username = computed(() => this.sesionActual()?.username ?? '');

  get token(): string | null {
    return this.sesionActual()?.token ?? null;
  }

  login(datos: LoginModel): Observable<AuthResponseModel> {
    return this.cliente.post<AuthResponseModel>(`${this.urlbase}/login`, datos).pipe(
      tap((res) => this.guardarSesion({ token: res.token, role: res.role, username: datos.username })),
    );
  }

  // Los siguientes endpoints responden texto plano
  registrar(datos: RegistroModel): Observable<string> {
    return this.cliente.post(`${this.urlbase}/register`, datos, { responseType: 'text' });
  }

  verificarEmail(token: string): Observable<string> {
    return this.cliente.get(`${this.urlbase}/verify`, { params: { token }, responseType: 'text' });
  }

  reenviarVerificacion(email: string): Observable<string> {
    const cuerpo: PasswordResetModel = { email };
    return this.cliente.post(`${this.urlbase}/resend-verification`, cuerpo, { responseType: 'text' });
  }

  solicitarRecuperacion(email: string): Observable<string> {
    const cuerpo: PasswordResetModel = { email };
    return this.cliente.post(`${this.urlbase}/forgot`, cuerpo, { responseType: 'text' });
  }

  restablecerPassword(token: string, nuevaPassword: string): Observable<string> {
    const cuerpo: PasswordResetModel = { token, nuevaPassword };
    return this.cliente.post(`${this.urlbase}/reset`, cuerpo, { responseType: 'text' });
  }

  logout(): void {
    try {
      localStorage.removeItem(CLAVE);
    } catch {
      /* almacenamiento no disponible */
    }
    this.sesionActual.set(null);
  }

  private guardarSesion(sesion: SesionModel): void {
    try {
      localStorage.setItem(CLAVE, JSON.stringify(sesion));
    } catch {
      /* almacenamiento no disponible */
    }
    this.sesionActual.set(sesion);
  }

  private leerSesion(): SesionModel | null {
    try {
      const crudo = localStorage.getItem(CLAVE);
      if (!crudo) return null;
      const sesion = JSON.parse(crudo) as SesionModel;
      return this.tokenVigente(sesion.token) ? sesion : null;
    } catch {
      return null;
    }
  }

  private tokenVigente(token: string): boolean {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const { exp } = JSON.parse(atob(base64));
      return !exp || exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
}
