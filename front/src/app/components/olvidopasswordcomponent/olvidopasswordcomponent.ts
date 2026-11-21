import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Authservice } from '../../services/authservice';
import { mensajeError } from '../../utils/httperror';

@Component({
  selector: 'app-olvidopasswordcomponent',
  standalone: false,
  templateUrl: './olvidopasswordcomponent.html',
  styleUrl: './olvidopasswordcomponent.css',
})
export class Olvidopasswordcomponent {
  private fb = inject(FormBuilder);
  private auth = inject(Authservice);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  cargando = signal(false);
  error = signal('');
  exito = signal('');

  invalido(): boolean {
    const c = this.form.controls.email;
    return c.invalid && (c.touched || c.dirty);
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.auth.solicitarRecuperacion(this.form.getRawValue().email).subscribe({
      next: (m) => {
        this.exito.set(m);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeError(e));
        this.cargando.set(false);
      },
    });
  }
}
