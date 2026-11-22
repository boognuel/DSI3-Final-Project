import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MensajeService } from '../../services/mensaje.service';

/**
 * Página que abre el enlace de verificación del correo y permite pedir uno nuevo.
 */
@Component({
  selector: 'app-verificaremailcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './verificaremailcomponent.css',
  templateUrl: './verificaremailcomponent.html',
})
export class Verificaremailcomponent implements OnInit {
  /** Estado. */
  estado: string = 'cargando';
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Correo electrónico. */
  email: string = '';
  /** Error del correo. */
  errorEmail: string = '';
  /** Resultado del reenvío. */
  mensajeReenvio: string = '';
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
   * Lee el token del enlace y verifica el correo.
   */
  ngOnInit(): void {
    const token: unknown = this.route.snapshot.queryParamMap.get('token');
    const error: string = this.validarToken(token);
    if (error !== '') {
      this.estado = 'error';
      this.mensaje = error;
      return;
    }
    this.auth.verificar(String(token)).subscribe({
      next: (texto: string) => {
        this.estado = 'ok';
        this.mensaje = texto;
      },
      error: (e: unknown) => {
        this.estado = 'error';
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Valida el token del enlace: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarToken(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'El enlace no trae el código de verificación.';
    }
    if (valor.length > 64 || !/^[A-Za-z0-9-]+$/.test(valor)) {
      return 'El enlace de verificación no es válido.';
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
      return 'Escribe tu correo.';
    }
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim())) {
      return 'Escribe un correo válido.';
    }
    return '';
  }

  /**
   * Valida el correo y pide un nuevo enlace.
   */
  reenviar(): void {
    this.mensajeReenvio = '';
    this.errorEmail = this.validarEmail(this.email);
    if (this.errorEmail !== '') {
      return;
    }
    this.enviando = true;
    this.auth.reenviarVerificacion(this.email.trim()).subscribe({
      next: (texto: string) => {
        this.enviando = false;
        this.mensajeReenvio = texto;
      },
      error: (e: unknown) => {
        this.enviando = false;
        this.mensajeReenvio = this.mensajes.deError(e);
      },
    });
  }
}
