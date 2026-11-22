import { ChangeDetectionStrategy, Component } from '@angular/core';
import { UbicacionService } from '../../services/ubicacion.service';

/**
 * Aviso que explica por qué se necesita la ubicación antes de que el navegador pida el permiso.
 */
@Component({
  selector: 'app-avisoubicacioncomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './avisoubicacioncomponent.css',
  templateUrl: './avisoubicacioncomponent.html',
})
export class Avisoubicacioncomponent {
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Indica si se están cargando los datos. */
  cargando: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param ubicacion Servicio de ubicación
   */
  constructor(public ubicacion: UbicacionService) {}

  /**
   * Pide la ubicación al navegador y muestra el error si no se obtiene.
   */
  permitir(): void {
    this.mensaje = '';
    this.cargando = true;
    this.ubicacion.obtener().then((error: string) => {
      this.cargando = false;
      this.mensaje = error;
    });
  }
}
