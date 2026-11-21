import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Authservice } from '../../services/authservice';
import { mensajeError } from '../../utils/httperror';

@Component({
  selector: 'app-logincomponent',
  standalone: false,
  templateUrl: './logincomponent.html',
  styleUrl: './logincomponent.css',
})
export class Logincomponent {
  private fb = inject(FormBuilder);
  private auth = inject(Authservice);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  cargando = signal(false);
  error = signal('');
  sinVerificar = signal(false);

  invalido(nombre: 'username' | 'password'): boolean {
    const c = this.form.controls[nombre];
    return c.invalid && (c.touched || c.dirty);
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.sinVerificar.set(false);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const destino = this.route.snapshot.queryParamMap.get('returnUrl');
        const seguro = destino && destino.startsWith('/') && !destino.startsWith('//') ? destino : '/';
        this.router.navigateByUrl(seguro);
      },
      error: (e) => {
        this.sinVerificar.set(e instanceof HttpErrorResponse && e.status === 403);
        this.error.set(mensajeError(e));
        this.cargando.set(false);
      },
    });
  }
}
