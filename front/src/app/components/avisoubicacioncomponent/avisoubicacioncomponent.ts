import { Component, inject } from '@angular/core';
import { Locationservice } from '../../services/locationservice';

@Component({
  selector: 'app-avisoubicacioncomponent',
  standalone: false,
  templateUrl: './avisoubicacioncomponent.html',
  styleUrl: './avisoubicacioncomponent.css',
})
export class Avisoubicacioncomponent {
  ubicacion = inject(Locationservice);
}
