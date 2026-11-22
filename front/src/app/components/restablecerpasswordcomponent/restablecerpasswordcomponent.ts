import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MensajeService } from '../../services/mensaje.service';

/**
 * Página que abre el enlace de recuperación para escribir la nueva contraseña.
 */
@Component({
  selector: 'app-restablecerpasswordcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './restablecerpasswordcomponent.css',
  templateUrl: './restablecerpasswordcomponent.html',
})
export class Restablecerpasswordcomponent implements OnInit {
  /** Token. */
  token: string = '';
  /** Contraseña. */
  password: string = '';
  /** Confirmación de la contraseña. */
  confirmacion: string = '';
  /** Error de la contraseña. */
  errorPassword: string = '';
  /** Error de la confirmación. */
  errorConfirmacion: string = '';
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Mensaje de éxito para el usuario. */
  exito: string = '';
  /** Indica si hay una petición en curso. */
  enviando: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param route Ruta activa
   * @param auth Servicio de autenticación
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Lee el token del enlace de recuperación.
   */
  ngOnInit(): void {
    const valor: unknown = this.route.snapshot.queryParamMap.get('token');
    if (
      typeof valor !== 'string' ||
      valor.trim() === '' ||
      valor.length > 64 ||
      !/^[A-Za-z0-9-]+$/.test(valor)
    ) {
      this.mensaje = 'El enlace de recuperación no es válido. Pide uno nuevo.';
      return;
    }
    this.token = valor;
  }

  /**
   * Valida la contraseña: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarPassword(valor: unknown): string {
    if (typeof valor !== 'string' || valor === '') {
      return 'Escribe la nueva contraseña.';
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
   * Valida y guarda la nueva contraseña.
   */
  guardar(): void {
    this.mensaje = '';
    this.errorPassword = this.validarPassword(this.password);
    this.errorConfirmacion = this.validarConfirmacion(this.confirmacion);
    if (this.token === '' || this.errorPassword !== '' || this.errorConfirmacion !== '') {
      return;
    }
    this.enviando = true;
    this.auth.restablecer(this.token, this.password).subscribe({
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
