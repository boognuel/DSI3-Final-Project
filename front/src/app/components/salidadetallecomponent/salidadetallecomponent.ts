import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { InvitacionModel } from '../../models/invitacion.model';
import { MarcadorModel } from '../../models/marcador.model';
import { SalidaModel } from '../../models/salida.model';
import { SalidaLugarModel } from '../../models/salidalugar.model';
import { FormatoService } from '../../services/formato.service';
import { InvitacionService } from '../../services/invitacion.service';
import { MensajeService } from '../../services/mensaje.service';
import { SalidaService } from '../../services/salida.service';
import { SesionService } from '../../services/sesion.service';

/**
 * Detalle de una salida: lugares propuestos, destino, invitados y mapa.
 */
@Component({
  selector: 'app-salidadetallecomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './salidadetallecomponent.css',
  templateUrl: './salidadetallecomponent.html',
})
export class Salidadetallecomponent implements OnInit {
  /** Salida. */
  salida: SalidaModel | null = null;
  /** Lugares propuestos. */
  lugares: SalidaLugarModel[] = [];
  /** Invitaciones enviadas. */
  invitaciones: InvitacionModel[] = [];
  /** Marcadores del mapa. */
  marcadores: MarcadorModel[] = [];
  /** Correo del invitado. */
  correo: string = '';
  /** Error del correo. */
  errorCorreo: string = '';
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';
  /** Indica si se pidió confirmar la cancelación. */
  confirmarCancelacion: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param route Ruta activa
   * @param router Enrutador
   * @param salidaService Servicio de salidas
   * @param invitacionService Servicio de invitaciones
   * @param sesion Servicio de sesión
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private route: ActivatedRoute,
    private router: Router,
    private salidaService: SalidaService,
    private invitacionService: InvitacionService,
    private sesion: SesionService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Valida el identificador de la URL y carga la salida.
   */
  ngOnInit(): void {
    const texto: unknown = this.route.snapshot.paramMap.get('id');
    const id: number = Number(texto);
    if (typeof texto !== 'string' || !Number.isInteger(id) || id <= 0) {
      this.mensaje = 'La salida pedida no es válida.';
      return;
    }
    this.cargar(id);
  }

  /**
   * Consulta la salida, sus lugares y sus invitaciones.
   *
   * @param id Salida
   */
  cargar(id: number): void {
    this.salidaService.getById(id).subscribe({
      next: (s: SalidaModel) => {
        if (typeof s !== 'object' || s === null || typeof s.titulo !== 'string') {
          this.mensaje = 'El servidor respondió con datos no válidos.';
          return;
        }
        this.salida = s;
        if (this.esOrganizador()) {
          this.cargarInvitaciones(id);
        }
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
    this.salidaService.lugares(id).subscribe({
      next: (lista: SalidaLugarModel[]) => {
        this.lugares = Array.isArray(lista) ? lista : [];
        const marcadores: MarcadorModel[] = [];
        for (const l of this.lugares) {
          marcadores.push({
            latitud: l.latitud,
            longitud: l.longitud,
            titulo: l.lugarNombre,
            destacado: l.seleccionado,
          });
        }
        this.marcadores = marcadores;
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Consulta las invitaciones de la salida.
   *
   * @param id Salida
   */
  cargarInvitaciones(id: number): void {
    this.invitacionService.porSalida(id).subscribe({
      next: (lista: InvitacionModel[]) => {
        this.invitaciones = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Indica si el usuario con sesión organiza la salida.
   *
   * @returns true si es el organizador
   */
  esOrganizador(): boolean {
    return this.salida !== null && this.salida.organizadorNombre === this.sesion.usuario();
  }

  /**
   * Indica si la salida todavía se puede modificar.
   *
   * @returns true si no está cancelada ni realizada
   */
  vigente(): boolean {
    return (
      this.salida !== null &&
      this.salida.estado !== 'CANCELADA' &&
      this.salida.estado !== 'REALIZADA'
    );
  }

  /**
   * Abre la búsqueda para proponer más lugares en esta salida.
   */
  agregarLugares(): void {
    if (this.salida === null) {
      return;
    }
    this.router.navigate(['/planes'], {
      queryParams: { salida: this.salida.id, presupuesto: this.salida.presupuesto },
    });
  }

  /**
   * Elige un lugar como destino de la salida.
   *
   * @param l Lugar propuesto
   */
  elegirDestino(l: SalidaLugarModel): void {
    this.mensaje = '';
    this.aviso = '';
    if (this.salida === null || this.salida.id === null) {
      return;
    }
    const id: number = this.salida.id;
    this.salidaService.seleccionar(id, l.lugarId).subscribe({
      next: (texto: string) => {
        this.aviso = texto;
        this.cargar(id);
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Valida el correo del invitado: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarCorreo(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'Escribe el correo de tu amigo.';
    }
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim())) {
      return 'Escribe un correo válido, por ejemplo nombre@dominio.com.';
    }
    return '';
  }

  /**
   * Envía la invitación por correo.
   */
  invitar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errorCorreo = this.validarCorreo(this.correo);
    if (this.errorCorreo !== '' || this.salida === null || this.salida.id === null) {
      return;
    }
    const id: number = this.salida.id;
    this.invitacionService.enviar(id, this.correo.trim()).subscribe({
      next: (texto: string) => {
        this.aviso = texto;
        this.correo = '';
        this.cargarInvitaciones(id);
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Cancela la salida cambiando su estado.
   */
  cancelar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.confirmarCancelacion = false;
    if (this.salida === null || this.salida.id === null) {
      return;
    }
    const id: number = this.salida.id;
    const datos: SalidaModel = {
      id: this.salida.id,
      titulo: this.salida.titulo,
      descripcion: this.salida.descripcion,
      organizadorId: this.salida.organizadorId,
      organizadorNombre: this.salida.organizadorNombre,
      presupuesto: this.salida.presupuesto,
      zonaNombre: this.salida.zonaNombre,
      latitudCentro: this.salida.latitudCentro,
      longitudCentro: this.salida.longitudCentro,
      radioMetros: this.salida.radioMetros,
      fechaHora: this.salida.fechaHora,
      estado: 'CANCELADA',
    };
    this.salidaService.actualizar(id, datos).subscribe({
      next: () => {
        this.aviso = 'La salida quedó cancelada.';
        this.cargar(id);
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
