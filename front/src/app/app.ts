import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Componente raíz: muestra la barra de navegación, la página de la ruta actual y el pie de página.
 */
@Component({
  selector: 'app-root',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {}
