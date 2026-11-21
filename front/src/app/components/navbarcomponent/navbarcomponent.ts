import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { SesionService } from '../../services/sesion.service';

/**
 * Barra de navegación con los enlaces según la sesión y el rol.
 */
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './navbarcomponent.css',
  templateUrl: './navbarcomponent.html',
})
export class Navbarcomponent {
  /**
   * Crea la instancia con sus dependencias.
   *
   * @param sesion Servicio de sesión
   * @param router Enrutador
   */
  constructor(
    public sesion: SesionService,
    private router: Router,
  ) {}

  /**
   * Cierra la sesión y vuelve al inicio de sesión.
   */
  salir(): void {
    this.sesion.cerrar();
    this.router.navigate(['/login']);
  }
}
