import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { SalidaModel } from '../../models/salida.model';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';
import { SalidaService } from '../../services/salida.service';

/**
 * Salidas de todos los usuarios, con filtro por estado y cancelación.
 */
@Component({
  selector: 'app-adminsalidascomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminsalidascomponent.css',
  templateUrl: './adminsalidascomponent.html',
})
export class Adminsalidascomponent implements OnInit {
  /** Salidas. */
  salidas: SalidaModel[] = [];
  /** Estado para filtrar; vacío para todos. */
  estado: string = '';
  /** Estados posibles. */
  estados: string[] = ['PLANEANDO', 'CONFIRMADA', 'CANCELADA', 'REALIZADA'];
  /** Salida que espera confirmación para cancelarse. */
  confirmar: number | null = null;
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param salidaService Servicio de salidas
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private salidaService: SalidaService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga las salidas al abrir la página.
   */
  ngOnInit(): void {
    this.cargar();
  }

  /**
   * Consulta las salidas de todos los usuarios.
   */
  cargar(): void {
    this.salidaService.getAll().subscribe({
      next: (lista: SalidaModel[]) => {
        this.salidas = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Valida el estado del filtro: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarEstado(valor: unknown): string {
    if (typeof valor !== 'string' || (valor !== '' && this.estados.indexOf(valor) < 0)) {
      return 'Elige un estado de la lista.';
    }
    return '';
  }

  /**
   * Filtra las salidas por el estado elegido.
   *
   * @returns Salidas que coinciden
   */
  filtradas(): SalidaModel[] {
    if (this.validarEstado(this.estado) !== '' || this.estado === '') {
      return this.salidas;
    }
    const lista: SalidaModel[] = [];
    for (const s of this.salidas) {
      if (s.estado === this.estado) {
        lista.push(s);
      }
    }
    return lista;
  }

  /**
   * Indica si una salida todavía se puede cancelar.
   *
   * @param s Salida
   * @returns true si no está cancelada ni realizada
   */
  cancelable(s: SalidaModel): boolean {
    return s.estado !== 'CANCELADA' && s.estado !== 'REALIZADA';
  }

  /**
   * Cancela una salida cambiando su estado.
   *
   * @param s Salida
   */
  cancelar(s: SalidaModel): void {
    this.mensaje = '';
    this.aviso = '';
    this.confirmar = null;
    if (s.id === null) {
      return;
    }
    const datos: SalidaModel = {
      id: s.id,
      titulo: s.titulo,
      descripcion: s.descripcion,
      organizadorId: s.organizadorId,
      organizadorNombre: s.organizadorNombre,
      presupuesto: s.presupuesto,
      zonaNombre: s.zonaNombre,
      latitudCentro: s.latitudCentro,
      longitudCentro: s.longitudCentro,
      radioMetros: s.radioMetros,
      fechaHora: s.fechaHora,
      estado: 'CANCELADA',
    };
    this.salidaService.actualizar(s.id, datos).subscribe({
      next: () => {
        this.aviso = 'La salida "' + s.titulo + '" quedó cancelada.';
        this.cargar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
