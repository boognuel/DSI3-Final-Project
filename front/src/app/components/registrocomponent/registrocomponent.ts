import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Authservice } from '../../services/authservice';
import { mensajeError } from '../../utils/httperror';
import { coinciden, validarPassword } from '../../utils/validadores';

@Component({
  selector: 'app-registrocomponent',
  standalone: false,
  templateUrl: './registrocomponent.html',
  styleUrl: './registrocomponent.css',
})
export class Registrocomponent {
  private fb = inject(FormBuilder);
  private auth = inject(Authservice);

  form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: ['', [Validators.required, validarPassword]],
      confirmacion: ['', Validators.required],
    },
    { validators: coinciden('password', 'confirmacion') },
  );

  cargando = signal(false);
  error = signal('');
  exito = signal('');

  invalido(nombre: 'username' | 'email' | 'password' | 'confirmacion'): boolean {
    const c = this.form.controls[nombre];
    return c.invalid && (c.touched || c.dirty);
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { username, email, password } = this.form.getRawValue();
    this.cargando.set(true);
    this.error.set('');

    this.auth.registrar({ username, email, password }).subscribe({
      next: (mensaje) => {
        this.exito.set(mensaje);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeError(e));
        this.cargando.set(false);
      },
    });
  }
}
