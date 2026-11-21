import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Authservice } from '../../services/authservice';
import { mensajeError } from '../../utils/httperror';

type Estado = 'verificando' | 'ok' | 'error' | 'sinToken';

@Component({
  selector: 'app-verificaremailcomponent',
  standalone: false,
  templateUrl: './verificaremailcomponent.html',
  styleUrl: './verificaremailcomponent.css',
})
export class Verificaremailcomponent implements OnInit {
  private route = inject(ActivatedRoute);
  private auth = inject(Authservice);
  private fb = inject(FormBuilder);

  estado = signal<Estado>('verificando');
  mensaje = signal('');
  reenvio = signal('');
  enviando = signal(false);

  email = this.fb.nonNullable.control('', [Validators.required, Validators.email]);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.estado.set('sinToken');
      return;
    }
    this.auth.verificarEmail(token).subscribe({
      next: (m) => {
        this.mensaje.set(m);
        this.estado.set('ok');
      },
      error: (e) => {
        this.mensaje.set(mensajeError(e));
        this.estado.set('error');
      },
    });
  }

  reenviar(): void {
    if (this.email.invalid) {
      this.email.markAsTouched();
      return;
    }
    this.enviando.set(true);
    this.auth.reenviarVerificacion(this.email.value).subscribe({
      next: (m) => {
        this.reenvio.set(m);
        this.enviando.set(false);
      },
      error: (e) => {
        this.reenvio.set(mensajeError(e));
        this.enviando.set(false);
      },
    });
  }
}
