import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { InvitacionModel } from '../../models/invitacion.model';
import { SalidaModel } from '../../models/salida.model';
import { FormatoService } from '../../services/formato.service';
import { InvitacionService } from '../../services/invitacion.service';
import { MensajeService } from '../../services/mensaje.service';
import { SalidaService } from '../../services/salida.service';

/**
 * Página con las salidas que organiza el usuario y las invitaciones que recibió.
 */
@Component({
  selector: 'app-salidascomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './salidascomponent.css',
  templateUrl: './salidascomponent.html',
})
export class Salidascomponent implements OnInit {
  /** Pestaña visible. */
  pestana: string = 'organizo';
  /** Salidas. */
  salidas: SalidaModel[] = [];
  /** Invitaciones. */
  invitaciones: InvitacionModel[] = [];
  /** Mensaje de error para el usuario. */
  mensaje: string = '';
  /** Mensaje de éxito para el usuario. */
  aviso: string = '';
  /** Indica si se están cargando los datos. */
  cargando: boolean = true;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param salidaService Servicio de salidas
   * @param invitacionService Servicio de invitaciones
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private salidaService: SalidaService,
    private invitacionService: InvitacionService,
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
    this.cargando = true;
    this.salidaService.mias().subscribe({
      next: (lista: SalidaModel[] | null) => {
        this.cargando = false;
        this.salidas = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.cargando = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
    this.invitacionService.mias().subscribe({
      next: (lista: InvitacionModel[] | null) => {
        this.invitaciones = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Elige la clase del distintivo según el estado.
   *
   * @param estado Estado de la salida o la invitación
   * @returns Clase de Bootstrap
   */
  claseEstado(estado: string | null): string {
    if (estado === 'ACEPTADA' || estado === 'CONFIRMADA') {
      return 'text-bg-success';
    }
    if (estado === 'RECHAZADA' || estado === 'CANCELADA' || estado === 'EXPIRADA') {
      return 'text-bg-secondary';
    }
    return 'badge-acento';
  }
}
