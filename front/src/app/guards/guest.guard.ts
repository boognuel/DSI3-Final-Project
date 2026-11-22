import { Injectable } from '@angular/core';
import { CanActivate, GuardResult, MaybeAsync, Router } from '@angular/router';
import { SesionService } from '../services/sesion.service';

/**
 * Guarda que solo deja pasar a quien no ha iniciado sesión, para las páginas de ingreso y registro.
 */
@Injectable({
  providedIn: 'root',
})
export class GuestGuard implements CanActivate {
  /**
   * Crea la instancia con sus dependencias.
   *
   * @param sesion Servicio de sesión
   * @param router Enrutador
   */
  constructor(
    private sesion: SesionService,
    private router: Router,
  ) {}

  /**
   * Decide si se puede entrar a la ruta.
   *
   * @returns true si no hay sesión, o la portada si ya la hay
   */
  canActivate(): MaybeAsync<GuardResult> {
    if (this.sesion.activa()) {
      return this.router.parseUrl('/');
    }
    return true;
  }
}
