import { Injectable } from '@angular/core';
import { AuthResponseModel } from '../models/authresponse.model';
import { SesionModel } from '../models/sesion.model';

/**
 * Guarda y lee la sesión del usuario (token, rol y nombre) en el almacenamiento del navegador.
 */
@Injectable({
  providedIn: 'root',
})
export class SesionService {
  /** Clave del almacenamiento donde se guarda la sesión. */
  private clave: string = 'bp_sesion';

  /**
   * Guarda la sesión en el almacenamiento del navegador.
   *
   * @param respuesta Respuesta del inicio de sesión
   * @returns true si se pudo guardar
   */
  guardar(respuesta: AuthResponseModel): boolean {
    const sesion: SesionModel = {
      token: respuesta.token,
      role: respuesta.role,
      username: respuesta.username,
    };
    try {
      localStorage.setItem(this.clave, JSON.stringify(sesion));
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Lee la sesión guardada y revisa su forma.
   *
   * @returns La sesión, o null si no hay una válida
   */
  leer(): SesionModel | null {
    let texto: string | null = null;
    try {
      texto = localStorage.getItem(this.clave);
    } catch {
      return null;
    }
    if (typeof texto !== 'string' || texto === '') {
      return null;
    }
    try {
      const datos: unknown = JSON.parse(texto);
      if (typeof datos !== 'object' || datos === null) {
        return null;
      }
      const sesion = datos as SesionModel;
      if (
        typeof sesion.token !== 'string' ||
        (sesion.role !== 'USER' && sesion.role !== 'ADMIN') ||
        typeof sesion.username !== 'string'
      ) {
        return null;
      }
      return sesion;
    } catch {
      return null;
    }
  }

  /**
   * Devuelve el token de la sesión.
   *
   * @returns Token, o vacío si no hay sesión
   */
  token(): string {
    const sesion = this.leer();
    return sesion === null ? '' : sesion.token;
  }

  /**
   * Devuelve el nombre del usuario de la sesión.
   *
   * @returns Nombre de usuario, o vacío si no hay sesión
   */
  usuario(): string {
    const sesion = this.leer();
    return sesion === null ? '' : sesion.username;
  }

  /**
   * Indica si hay sesión y el token no ha vencido.
   *
   * @returns true si la sesión está activa
   */
  activa(): boolean {
    const token = this.token();
    if (token === '') {
      return false;
    }
    const partes: string[] = token.split('.');
    if (partes.length !== 3) {
      return false;
    }
    try {
      const base: string = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const datos: unknown = JSON.parse(atob(base));
      if (typeof datos !== 'object' || datos === null) {
        return false;
      }
      const exp: unknown = (datos as { exp: unknown }).exp;
      if (typeof exp !== 'number') {
        return false;
      }
      return exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  /**
   * Indica si la sesión activa es de un administrador.
   *
   * @returns true si es administrador
   */
  esAdmin(): boolean {
    const sesion = this.leer();
    return this.activa() && sesion !== null && sesion.role === 'ADMIN';
  }

  /**
   * Borra la sesión del navegador.
   *
   * @returns true si se pudo borrar
   */
  cerrar(): boolean {
    try {
      localStorage.removeItem(this.clave);
      return true;
    } catch {
      return false;
    }
  }
}
