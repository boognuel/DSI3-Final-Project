import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import {
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  DoughnutController,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js';
import { ConteoModel } from '../../models/conteo.model';
import { EstadisticasModel } from '../../models/estadisticas.model';
import { AdminService } from '../../services/admin.service';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';

Chart.register(
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  DoughnutController,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
);
Chart.defaults.color = '#c4b6d8';
Chart.defaults.borderColor = 'rgba(243, 231, 235, 0.14)';

/**
 * Estadísticas con gráficas y descarga del reporte en Excel.
 */
@Component({
  selector: 'app-adminestadisticascomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminestadisticascomponent.css',
  templateUrl: './adminestadisticascomponent.html',
})
export class Adminestadisticascomponent implements AfterViewInit, OnDestroy {
  /** Lienzo de usuarios por mes. */
  @ViewChild('usuarios') usuarios!: ElementRef<HTMLCanvasElement>;
  /** Lienzo de lugares por categoría. */
  @ViewChild('lugares') lugares!: ElementRef<HTMLCanvasElement>;
  /** Lienzo de invitaciones por estado. */
  @ViewChild('invitaciones') invitaciones!: ElementRef<HTMLCanvasElement>;

  /** Fecha inicial del reporte. */
  desde: string = '';
  /** Fecha final del reporte. */
  hasta: string = '';
  /** Error de la fecha inicial. */
  errorDesde: string = '';
  /** Error de la fecha final. */
  errorHasta: string = '';
  /** Mensaje de error. */
  mensaje: string = '';
  /** Indica si hay una descarga en curso. */
  descargando: boolean = false;
  /** Gráficas dibujadas. */
  private graficas: Chart[] = [];
  /** Colores de la gráfica circular. */
  private colores: string[] = ['#feca03', '#4cc38a', '#e85d8a', '#c4b6d8'];

  /**
   * Crea la instancia con sus dependencias y propone el último mes para el reporte.
   *
   * @param formato Servicio de formato
   * @param adminService Servicio de administración
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private adminService: AdminService,
    private mensajes: MensajeService,
  ) {
    const hoy: Date = new Date();
    this.hasta = this.aTexto(hoy);
    this.desde = this.aTexto(new Date(hoy.getTime() - 30 * 24 * 60 * 60 * 1000));
  }

  /**
   * Consulta las estadísticas cuando los lienzos ya existen.
   */
  ngAfterViewInit(): void {
    this.adminService.estadisticas().subscribe({
      next: (e: EstadisticasModel) => {
        if (typeof e !== 'object' || e === null || !Array.isArray(e.usuariosPorMes)) {
          this.mensaje = 'El servidor respondió con datos no válidos.';
          return;
        }
        this.dibujar(e);
      },
      error: (err: unknown) => {
        this.mensaje = this.mensajes.deError(err);
      },
    });
  }

  /**
   * Libera las gráficas al salir de la página.
   */
  ngOnDestroy(): void {
    for (const g of this.graficas) {
      g.destroy();
    }
    this.graficas = [];
  }

  /**
   * Convierte una fecha al formato aaaa-mm-dd.
   *
   * @param fecha Fecha
   * @returns Texto de la fecha
   */
  aTexto(fecha: Date): string {
    const mes: number = fecha.getMonth() + 1;
    const dia: number = fecha.getDate();
    return (
      fecha.getFullYear() + '-' + (mes < 10 ? '0' + mes : mes) + '-' + (dia < 10 ? '0' + dia : dia)
    );
  }

  /**
   * Valida una fecha: primero el tipo y después el contenido.
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
   * Valida el periodo y descarga el reporte en Excel.
   */
  descargar(): void {
    this.mensaje = '';
    this.errorDesde = this.validarFecha(this.desde);
    this.errorHasta = this.validarFecha(this.hasta);
    if (this.errorDesde === '' && this.errorHasta === '' && this.hasta < this.desde) {
      this.errorHasta = 'Debe ser posterior a la fecha inicial.';
    }
    if (this.errorDesde !== '' || this.errorHasta !== '') {
      return;
    }
    this.descargando = true;
    this.adminService.reporte(this.desde, this.hasta).subscribe({
      next: (archivo: Blob) => {
        this.descargando = false;
        const enlace: HTMLAnchorElement = document.createElement('a');
        enlace.href = URL.createObjectURL(archivo);
        enlace.download = 'reporte-bogoparche-' + this.hasta + '.xlsx';
        enlace.click();
        URL.revokeObjectURL(enlace.href);
      },
      error: () => {
        this.descargando = false;
        this.mensaje = 'No se pudo descargar el reporte. Revisa el periodo e intenta de nuevo.';
      },
    });
  }

  /**
   * Saca las etiquetas de una serie.
   *
   * @param serie Serie
   * @returns Etiquetas
   */
  private etiquetas(serie: ConteoModel[]): string[] {
    const lista: string[] = [];
    for (const p of serie) {
      lista.push(this.formato.estado(p.etiqueta));
    }
    return lista;
  }

  /**
   * Saca los totales de una serie.
   *
   * @param serie Serie
   * @returns Totales
   */
  private totales(serie: ConteoModel[]): number[] {
    const lista: number[] = [];
    for (const p of serie) {
      lista.push(p.total);
    }
    return lista;
  }

  /**
   * Dibuja las tres gráficas.
   *
   * @param e Estadísticas
   */
  private dibujar(e: EstadisticasModel): void {
    this.graficas.push(
      new Chart(this.usuarios.nativeElement, {
        type: 'line',
        data: {
          labels: this.etiquetas(e.usuariosPorMes),
          datasets: [
            {
              label: 'Usuarios',
              data: this.totales(e.usuariosPorMes),
              borderColor: '#feca03',
              backgroundColor: '#feca03',
              tension: 0.3,
            },
          ],
        },
        options: {
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
        },
      }),
    );
    this.graficas.push(
      new Chart(this.lugares.nativeElement, {
        type: 'bar',
        data: {
          labels: this.etiquetas(e.lugaresPorCategoria),
          datasets: [
            {
              label: 'Lugares',
              data: this.totales(e.lugaresPorCategoria),
              backgroundColor: '#e85d8a',
              borderRadius: 6,
            },
          ],
        },
        options: {
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
        },
      }),
    );
    this.graficas.push(
      new Chart(this.invitaciones.nativeElement, {
        type: 'doughnut',
        data: {
          labels: this.etiquetas(e.invitacionesPorEstado),
          datasets: [
            {
              data: this.totales(e.invitacionesPorEstado),
              backgroundColor: this.colores,
              borderColor: '#1d0a4a',
              borderWidth: 2,
            },
          ],
        },
        options: { plugins: { legend: { position: 'bottom' } } },
      }),
    );
  }
}
