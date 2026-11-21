import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Pie de página.
 */
@Component({
  selector: 'app-footercomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './footercomponent.css',
  templateUrl: './footercomponent.html',
})
export class Footercomponent {
  /** Año actual. */
  anio: number = new Date().getFullYear();
}
