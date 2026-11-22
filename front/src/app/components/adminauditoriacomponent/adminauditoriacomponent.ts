import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { AuditoriaModel } from '../../models/auditoria.model';
import { PaginaModel } from '../../models/pagina.model';
import { AdminService } from '../../services/admin.service';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';

/**
 * Página de consulta de la auditoría.
 */
@Component({
  selector: 'app-adminauditoriacomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminauditoriacomponent.css',
  templateUrl: './adminauditoriacomponent.html',
})
export class Adminauditoriacomponent implements OnInit {
  /** Página actual de resultados. */
  pagina: PaginaModel<AuditoriaModel> | null = null;
  /** Usuario para filtrar. */
  usuario: string = '';
  /** Acción para filtrar. */
  accion: string = '';
  /** Fecha inicial del periodo. */
  desde: string = '';
  /** Fecha final del periodo. */
  hasta: string = '';
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Acciones que se pueden filtrar. */
  acciones: string[] = [
    'LOGIN_EXITOSO',
    'LOGIN_FALLIDO',
    'REGISTRO',
    'VERIFICAR_EMAIL',
    'SOLICITAR_RECUPERACION',
    'CAMBIAR_PASSWORD',
    'CREAR_SALIDA',
    'EDITAR_SALIDA',
    'ELIMINAR_SALIDA',
    'ENVIAR_INVITACION',
    'ACEPTAR_INVITACION',
    'RECHAZAR_INVITACION',
    'BUSCAR_LUGARES',
    'AGREGAR_LUGAR',
    'CALCULAR_RUTA',
    'DESCARGAR_REPORTE',
    'CONSULTAR_AUDITORIA',
    'GESTION_USUARIO',
    'GESTION_CATALOGO',
  ];

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param adminService Servicio de administración
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private adminService: AdminService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga los datos iniciales de la página.
   */
  ngOnInit(): void {
    const hoy: Date = new Date();
    this.hasta = this.aTexto(hoy);
    this.desde = this.aTexto(new Date(hoy.getTime() - 7 * 24 * 60 * 60 * 1000));
    this.buscar(0);
  }

  /**
   * Convierte una fecha al formato aaaa-mm-dd.
   *
   * @param fecha Fecha
   * @returns Fecha en texto
   */
  aTexto(fecha: Date): string {
    const mes: number = fecha.getMonth() + 1;
    const dia: number = fecha.getDate();
    return (
      fecha.getFullYear() + '-' + (mes < 10 ? '0' + mes : mes) + '-' + (dia < 10 ? '0' + dia : dia)
    );
  }

  /**
   * Valida el usuario del filtro: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarUsuario(valor: unknown): string {
    if (typeof valor !== 'string') {
      return 'El usuario no es válido.';
    }
    if (valor.trim().length > 100) {
      return 'Escribe hasta 100 caracteres.';
    }
    return '';
  }

  /**
   * Valida la acción: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarAccion(valor: unknown): string {
    if (typeof valor !== 'string' || (valor !== '' && this.acciones.indexOf(valor) < 0)) {
      return 'Elige una acción de la lista.';
    }
    return '';
  }

  /**
   * Valida la fecha: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarFecha(valor: unknown): string {
    if (
      typeof valor !== 'string' ||
      !/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(valor) ||
      Number.isNaN(new Date(valor).getTime())
    ) {
      return 'Elige una fecha válida.';
    }
    return '';
  }

  /**
   * Valida los filtros y consulta una página de la auditoría.
   *
   * @param numero Número de página, desde 0
   */
  buscar(numero: number): void {
    this.mensaje = '';
    this.errores = {
      usuario: this.validarUsuario(this.usuario),
      accion: this.validarAccion(this.accion),
      desde: this.validarFecha(this.desde),
      hasta: this.validarFecha(this.hasta),
    };
    if (this.errores['desde'] === '' && this.errores['hasta'] === '' && this.hasta < this.desde) {
      this.errores['hasta'] = 'Debe ser posterior a la fecha inicial.';
    }
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    this.adminService
      .auditoria(this.usuario.trim(), this.accion, this.desde, this.hasta, numero)
      .subscribe({
        next: (p: PaginaModel<AuditoriaModel>) => {
          if (typeof p !== 'object' || p === null || !Array.isArray(p.content)) {
            this.mensaje = 'El servidor respondió con datos no válidos.';
            return;
          }
          this.pagina = p;
        },
        error: (e: unknown) => {
          this.mensaje = this.mensajes.deError(e);
        },
      });
  }
}
