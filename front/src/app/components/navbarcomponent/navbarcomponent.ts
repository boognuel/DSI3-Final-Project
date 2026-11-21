import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Authservice } from '../../services/authservice';

@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  templateUrl: './navbarcomponent.html',
  styleUrl: './navbarcomponent.css',
})
export class Navbarcomponent {
  auth = inject(Authservice);
  private router = inject(Router);

  abierto = signal(false);

  alternar(): void {
    this.abierto.update((v) => !v);
  }

  cerrar(): void {
    this.abierto.set(false);
  }

  salir(): void {
    this.auth.logout();
    this.cerrar();
    this.router.navigate(['/']);
  }
}
