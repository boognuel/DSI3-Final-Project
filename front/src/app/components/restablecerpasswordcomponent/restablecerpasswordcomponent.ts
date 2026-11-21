import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Authservice } from '../../services/authservice';
import { mensajeError } from '../../utils/httperror';
import { coinciden, validarPassword } from '../../utils/validadores';

@Component({
  selector: 'app-restablecerpasswordcomponent',
  standalone: false,
  templateUrl: './restablecerpasswordcomponent.html',
  styleUrl: './restablecerpasswordcomponent.css',
})
export class Restablecerpasswordcomponent {
  private fb = inject(FormBuilder);
  private auth = inject(Authservice);
  private route = inject(ActivatedRoute);

  token = this.route.snapshot.queryParamMap.get('token') ?? '';

  form = this.fb.nonNullable.group(
    {
      nuevaPassword: ['', [Validators.required, validarPassword]],
      confirmacion: ['', Validators.required],
    },
    { validators: coinciden('nuevaPassword', 'confirmacion') },
  );

  cargando = signal(false);
  error = signal('');
  exito = signal('');

  invalido(nombre: 'nuevaPassword' | 'confirmacion'): boolean {
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
    this.auth.restablecerPassword(this.token, this.form.getRawValue().nuevaPassword).subscribe({
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
