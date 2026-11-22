import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthResponseModel } from '../../models/authresponse.model';
import { AuthService } from '../../services/auth.service';
import { MensajeService } from '../../services/mensaje.service';
import { SesionService } from '../../services/sesion.service';

/**
 * Página de inicio de sesión.
 */
@Component({
  selector: 'app-logincomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './logincomponent.css',
  templateUrl: './logincomponent.html',
})
export class Logincomponent {
  /** Nombre de usuario. */
  username: string = '';
  /** Contraseña. */
  password: string = '';
  /** Error del usuario. */
  errorUsername: string = '';
  /** Error de la contraseña. */
  errorPassword: string = '';
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Indica si hay una petición en curso. */
  enviando: boolean = false;
  /** Indica si la cuenta existe pero no ha verificado el correo. */
  sinVerificar: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param auth Servicio de autenticación
   * @param sesion Servicio de sesión
   * @param mensajes Servicio de mensajes de error
   * @param router Enrutador
   */
  constructor(
    private auth: AuthService,
    private sesion: SesionService,
    private mensajes: MensajeService,
    private router: Router,
  ) {}

  /**
   * Valida el nombre de usuario: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarUsername(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'Escribe tu nombre de usuario.';
    }
    if (!/^[A-Za-z0-9._]+$/.test(valor.trim())) {
      return 'El usuario solo tiene letras sin tilde, números, punto o guion bajo.';
    }
    return '';
  }

  /**
   * Valida la contraseña: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarPassword(valor: unknown): string {
    if (typeof valor !== 'string' || valor === '') {
      return 'Escribe tu contraseña.';
    }
    return '';
  }

  /**
   * Valida los campos e inicia sesión.
   */
  ingresar(): void {
    this.mensaje = '';
    this.sinVerificar = false;
    this.errorUsername = this.validarUsername(this.username);
    this.errorPassword = this.validarPassword(this.password);
    if (this.errorUsername !== '' || this.errorPassword !== '') {
      return;
    }
    this.enviando = true;
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: (respuesta: AuthResponseModel) => {
        this.enviando = false;
        if (
          typeof respuesta !== 'object' ||
          respuesta === null ||
          typeof respuesta.token !== 'string'
        ) {
          this.mensaje = 'El servidor respondió con datos no válidos.';
          return;
        }
        if (!this.sesion.guardar(respuesta, this.username.trim())) {
          this.mensaje =
            'No se pudo guardar la sesión en este navegador. Revisa que permita guardar datos.';
          return;
        }
        this.router.navigate(['/planes']);
      },
      error: (error: unknown) => {
        this.enviando = false;
        this.sinVerificar = error instanceof HttpErrorResponse && error.status === 403;
        this.mensaje = this.mensajes.deError(error);
      },
    });
  }
}
