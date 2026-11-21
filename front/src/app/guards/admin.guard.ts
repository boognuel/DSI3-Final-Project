import { Injectable } from '@angular/core';
import { CanActivate, GuardResult, MaybeAsync, Router } from '@angular/router';
import { SesionService } from '../services/sesion.service';

/**
 * Guarda que solo deja pasar a administradores con sesión activa.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminGuard implements CanActivate {
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
   * @returns true si puede entrar, o la ruta a la que se redirige
   */
  canActivate(): MaybeAsync<GuardResult> {
    if (this.sesion.esAdmin()) {
      return true;
    }
    if (this.sesion.activa()) {
      return this.router.parseUrl('/inicio');
    }
    return this.router.parseUrl('/login');
  }
}
