import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Franja del modo administrador con los enlaces a sus secciones.
 */
@Component({
  selector: 'app-adminbarracomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminbarracomponent.css',
  templateUrl: './adminbarracomponent.html',
})
export class Adminbarracomponent {}
