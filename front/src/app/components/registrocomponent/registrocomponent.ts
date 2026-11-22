import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { MensajeService } from '../../services/mensaje.service';
import { RegistroModel } from '../../models/registro.model';

/**
 * Página de registro de cuentas nuevas.
 */
@Component({
  selector: 'app-registrocomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './registrocomponent.css',
  templateUrl: './registrocomponent.html',
})
export class Registrocomponent {
  /** Nombre de usuario. */
  username: string = '';
  /** Correo electrónico. */
  email: string = '';
  /** Contraseña. */
  password: string = '';
  /** Confirmación de la contraseña. */
  confirmacion: string = '';
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
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
   * Valida el nombre de usuario: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarUsername(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'El nombre de usuario es obligatorio.';
    }
    const limpio: string = valor.trim();
    if (limpio.length < 3 || limpio.length > 50) {
      return 'Debe tener entre 3 y 50 caracteres.';
    }
    return '';
  }

  /**
   * Valida el correo: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarEmail(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'El correo es obligatorio.';
    }
    if (valor.trim().length > 150 || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim())) {
      return 'Escribe un correo válido, por ejemplo nombre@dominio.com.';
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
      return 'La contraseña es obligatoria.';
    }
    if (
      valor.length < 8 ||
      !/[a-z]/.test(valor) ||
      !/[A-Z]/.test(valor) ||
      !/[0-9]/.test(valor) ||
      !/[^A-Za-z0-9]/.test(valor)
    ) {
      return 'Mínimo 8 caracteres, con mayúscula, minúscula, número y símbolo.';
    }
    return '';
  }

  /**
   * Valida la confirmación de la contraseña: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarConfirmacion(valor: unknown): string {
    if (typeof valor !== 'string' || valor === '') {
      return 'Confirma la contraseña.';
    }
    if (valor !== this.password) {
      return 'Las contraseñas no coinciden.';
    }
    return '';
  }

  /**
   * Valida el formulario y registra la cuenta.
   */
  registrar(): void {
    this.mensaje = '';
    this.exito = '';
    this.errores = {
      username: this.validarUsername(this.username),
      email: this.validarEmail(this.email),
      password: this.validarPassword(this.password),
      confirmacion: this.validarConfirmacion(this.confirmacion),
    };
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    const datos: RegistroModel = {
      username: this.username.trim(),
      email: this.email.trim(),
      password: this.password,
    };
    this.enviando = true;
    this.auth.registrar(datos).subscribe({
      next: (texto: string) => {
        this.enviando = false;
        this.exito = typeof texto === 'string' && texto !== '' ? texto : 'Cuenta creada.';
      },
      error: (error: unknown) => {
        this.enviando = false;
        this.mensaje = this.mensajes.deError(error);
      },
    });
  }
}
