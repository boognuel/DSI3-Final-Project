import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { MensajeService } from '../../services/mensaje.service';

/**
 * Página para pedir el enlace de recuperación de la contraseña.
 */
@Component({
  selector: 'app-olvidopasswordcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './olvidopasswordcomponent.css',
  templateUrl: './olvidopasswordcomponent.html',
})
export class Olvidopasswordcomponent {
  /** Correo electrónico. */
  email: string = '';
  /** Error del correo. */
  errorEmail: string = '';
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Mensaje de éxito para el usuario. */
  exito: string = '';
  /** Indica si hay una petición en curso. */
  enviando: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param auth Servicio de autenticación
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    private auth: AuthService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Valida el correo: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarEmail(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'Escribe tu correo.';
    }
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim())) {
      return 'Escribe un correo válido.';
    }
    return '';
  }

  /**
   * Valida el correo y pide el enlace de recuperación.
   */
  enviar(): void {
    this.mensaje = '';
    this.exito = '';
    this.errorEmail = this.validarEmail(this.email);
    if (this.errorEmail !== '') {
      return;
    }
    this.enviando = true;
    this.auth.olvido(this.email.trim()).subscribe({
      next: (texto: string) => {
        this.enviando = false;
        this.exito = texto;
      },
      error: (e: unknown) => {
        this.enviando = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
