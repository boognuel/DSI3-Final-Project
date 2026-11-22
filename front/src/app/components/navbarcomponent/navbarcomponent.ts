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
  /** Indica si el menú está abierto en pantallas pequeñas. */
  abierto: boolean = false;

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
   * Abre o cierra el menú en pantallas pequeñas.
   */
  alternar(): void {
    this.abierto = !this.abierto;
  }

  /**
   * Cierra el menú en pantallas pequeñas.
   */
  cerrar(): void {
    this.abierto = false;
  }

  /**
   * Cierra la sesión y vuelve al inicio de sesión.
   */
  salir(): void {
    this.cerrar();
    this.sesion.cerrar();
    this.router.navigate(['/login']);
  }
}
