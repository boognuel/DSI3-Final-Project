import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { UsuarioModel } from '../../models/usuario.model';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';
import { UsuarioService } from '../../services/usuario.service';

/**
 * Página de administración de usuarios.
 */
@Component({
  selector: 'app-adminusuarioscomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminusuarioscomponent.css',
  templateUrl: './adminusuarioscomponent.html',
})
export class Adminusuarioscomponent implements OnInit {
  /** Usuarios. */
  usuarios: UsuarioModel[] = [];
  /** Texto para filtrar la tabla. */
  filtro: string = '';
  /** Identificador del registro en edición, o null si se está creando uno. */
  editando: number | null = null;
  /** Nombre de usuario. */
  username: string = '';
  /** Correo electrónico. */
  email: string = '';
  /** Contraseña. */
  password: string = '';
  /** Rol del usuario. */
  role: string = 'USER';
  /** Indica si la cuenta está habilitada. */
  enabled: boolean = true;
  /** Indica si el correo está verificado. */
  emailVerificado: boolean = true;
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Mensaje de éxito para el usuario. */
  aviso: string = '';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param usuarioService Servicio de usuarios
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private usuarioService: UsuarioService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga los datos iniciales de la página.
   */
  ngOnInit(): void {
    this.cargar();
  }

  /**
   * Consulta los datos y los muestra.
   */
  cargar(): void {
    this.usuarioService.getAll().subscribe({
      next: (lista: UsuarioModel[] | null) => {
        this.usuarios = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Filtra los usuarios por nombre o correo.
   *
   * @returns Usuarios que coinciden
   */
  filtrados(): UsuarioModel[] {
    const texto: string = this.filtro.trim().toLowerCase();
    if (texto === '') {
      return this.usuarios;
    }
    const lista: UsuarioModel[] = [];
    for (const u of this.usuarios) {
      if (u.username.toLowerCase().includes(texto) || u.email.toLowerCase().includes(texto)) {
        lista.push(u);
      }
    }
    return lista;
  }

  /**
   * Limpia el formulario para crear un registro.
   */
  nuevo(): void {
    this.editando = null;
    this.username = '';
    this.email = '';
    this.password = '';
    this.role = 'USER';
    this.enabled = true;
    this.emailVerificado = true;
    this.errores = {};
  }

  /**
   * Carga un registro en el formulario.
   *
   * @param u Usuario
   */
  editar(u: UsuarioModel): void {
    this.editando = u.id;
    this.username = u.username;
    this.email = u.email;
    this.password = '';
    this.role = u.role;
    this.enabled = u.enabled;
    this.emailVerificado = u.emailVerificado;
    this.errores = {};
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
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim())) {
      return 'Escribe un correo válido.';
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
    if (typeof valor !== 'string') {
      return 'La contraseña no es válida.';
    }
    if (valor === '' && this.editando !== null) {
      return '';
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
   * Valida el rol: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarRol(valor: unknown): string {
    if (valor !== 'USER' && valor !== 'ADMIN') {
      return 'Elige un rol.';
    }
    return '';
  }

  /**
   * Valida el formulario y envía los datos.
   */
  guardar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errores = {
      username: this.validarUsername(this.username),
      email: this.validarEmail(this.email),
      password: this.validarPassword(this.password),
      role: this.validarRol(this.role),
    };
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    const datos: UsuarioModel = {
      id: this.editando,
      username: this.username.trim(),
      email: this.email.trim(),
      password: this.password === '' ? null : this.password,
      role: this.role,
      enabled: this.enabled,
      emailVerificado: this.emailVerificado,
    };
    const peticion =
      this.editando === null
        ? this.usuarioService.crear(datos)
        : this.usuarioService.actualizar(this.editando, datos);
    peticion.subscribe({
      next: (texto: string) => {
        this.aviso = texto;
        this.nuevo();
        this.cargar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Elimina un usuario.
   *
   * @param u Usuario
   */
  eliminar(u: UsuarioModel): void {
    this.mensaje = '';
    this.aviso = '';
    if (u.id === null) {
      return;
    }
    this.usuarioService.eliminar(u.id).subscribe({
      next: (texto: string) => {
        this.aviso = texto;
        this.cargar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
