import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UsuarioModel } from '../../models/usuario.model';
import { MensajeService } from '../../services/mensaje.service';
import { PerfilService } from '../../services/perfil.service';
import { SesionService } from '../../services/sesion.service';

/**
 * Perfil del usuario: datos de la cuenta y cambio de contraseña.
 */
@Component({
  selector: 'app-perfilcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './perfilcomponent.css',
  templateUrl: './perfilcomponent.html',
})
export class Perfilcomponent implements OnInit {
  /** Datos del usuario. */
  perfil: UsuarioModel | null = null;
  /** Nombre de usuario del formulario. */
  username: string = '';
  /** Correo del formulario. */
  email: string = '';
  /** Contraseña actual. */
  actual: string = '';
  /** Contraseña nueva. */
  nueva: string = '';
  /** Confirmación de la contraseña nueva. */
  confirmacion: string = '';
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param perfilService Servicio del perfil
   * @param sesion Servicio de sesión
   * @param mensajes Servicio de mensajes de error
   * @param router Enrutador
   */
  constructor(
    private perfilService: PerfilService,
    private sesion: SesionService,
    private mensajes: MensajeService,
    private router: Router,
  ) {}

  /**
   * Carga los datos al abrir la página.
   */
  ngOnInit(): void {
    this.cargar();
  }

  /**
   * Consulta el perfil y llena el formulario.
   */
  cargar(): void {
    this.perfilService.get().subscribe({
      next: (u: UsuarioModel) => {
        if (typeof u !== 'object' || u === null || typeof u.username !== 'string') {
          this.mensaje = 'El servidor respondió con datos no válidos.';
          return;
        }
        this.perfil = u;
        this.username = u.username;
        this.email = typeof u.email === 'string' ? u.email : '';
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

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
    if (valor.trim().length < 3 || valor.trim().length > 50) {
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
   * Valida la contraseña actual: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarActual(valor: unknown): string {
    if (typeof valor !== 'string' || valor === '') {
      return 'Escribe tu contraseña actual.';
    }
    return '';
  }

  /**
   * Valida la contraseña nueva: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarNueva(valor: unknown): string {
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
    if (valor === this.actual) {
      return 'Debe ser distinta de la actual.';
    }
    return '';
  }

  /**
   * Valida la confirmación: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarConfirmacion(valor: unknown): string {
    if (typeof valor !== 'string' || valor === '') {
      return 'Confirma la nueva contraseña.';
    }
    if (valor !== this.nueva) {
      return 'Las contraseñas no coinciden.';
    }
    return '';
  }

  /**
   * Valida y guarda el nombre de usuario y el correo. Si cambia el nombre de usuario, la sesión
   * se cierra para volver a entrar con el nuevo.
   */
  guardarDatos(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errores['username'] = this.validarUsername(this.username);
    this.errores['email'] = this.validarEmail(this.email);
    if (this.errores['username'] !== '' || this.errores['email'] !== '') {
      return;
    }
    const cambioNombre: boolean =
      this.perfil !== null && this.username.trim() !== this.perfil.username;
    this.perfilService.actualizar(this.username.trim(), this.email.trim()).subscribe({
      next: (texto: string) => {
        if (cambioNombre) {
          this.sesion.cerrar();
          this.router.navigate(['/login']);
          return;
        }
        this.aviso = texto;
        this.cargar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Valida y cambia la contraseña.
   */
  cambiarPassword(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errores['actual'] = this.validarActual(this.actual);
    this.errores['nueva'] = this.validarNueva(this.nueva);
    this.errores['confirmacion'] = this.validarConfirmacion(this.confirmacion);
    if (
      this.errores['actual'] !== '' ||
      this.errores['nueva'] !== '' ||
      this.errores['confirmacion'] !== ''
    ) {
      return;
    }
    this.perfilService.cambiarPassword({ actual: this.actual, nueva: this.nueva }).subscribe({
      next: (texto: string) => {
        this.aviso = texto;
        this.actual = '';
        this.nueva = '';
        this.confirmacion = '';
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
