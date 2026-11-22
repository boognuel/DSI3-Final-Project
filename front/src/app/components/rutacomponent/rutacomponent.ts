import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MarcadorModel } from '../../models/marcador.model';
import { MedioModel } from '../../models/medio.model';
import { RutaModel } from '../../models/ruta.model';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';
import { RutaService } from '../../services/ruta.service';
import { UbicacionService } from '../../services/ubicacion.service';

/**
 * Ruta desde la ubicación del usuario hasta un lugar de una salida, con las indicaciones.
 */
@Component({
  selector: 'app-rutacomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './rutacomponent.css',
  templateUrl: './rutacomponent.html',
})
export class Rutacomponent implements OnInit {
  /** Salida. */
  salidaId: number = 0;
  /** Lugar de destino. */
  lugarId: number = 0;
  /** Modo de transporte. */
  modo: string = 'PIE';
  /** Ruta calculada. */
  ruta: RutaModel | null = null;
  /** Puntos de la línea del camino como latitud, longitud. */
  puntos: number[][] = [];
  /** Marcadores del mapa. */
  marcadores: MarcadorModel[] = [];
  /** Mensaje de error. */
  mensaje: string = '';
  /** Indica si hay un cálculo en curso. */
  calculando: boolean = false;
  /** Modos de transporte. */
  modos: MedioModel[] = [
    { valor: 'PIE', texto: 'A pie', icono: 'person-walking' },
    { valor: 'BICICLETA', texto: 'Bicicleta', icono: 'person-biking' },
    { valor: 'CARRO', texto: 'Carro', icono: 'car' },
  ];

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param ubicacion Servicio de ubicación
   * @param route Ruta activa
   * @param rutaService Servicio de rutas
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    public ubicacion: UbicacionService,
    private route: ActivatedRoute,
    private rutaService: RutaService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Valida los identificadores de la URL y calcula la ruta.
   */
  ngOnInit(): void {
    const salida: number = this.validarId(this.route.snapshot.paramMap.get('salidaId'));
    const lugar: number = this.validarId(this.route.snapshot.paramMap.get('lugarId'));
    if (salida <= 0 || lugar <= 0) {
      this.mensaje = 'La ruta pedida no es válida.';
      return;
    }
    this.salidaId = salida;
    this.lugarId = lugar;
    if (this.ubicacion.tieneUbicacion()) {
      this.calcular();
    } else {
      this.ubicacion.obtener().then((error: string) => {
        if (error === '') {
          this.calcular();
        } else {
          this.mensaje = error;
        }
      });
    }
  }

  /**
   * Valida un identificador de la URL: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns El número, o 0 si no es válido
   */
  validarId(valor: unknown): number {
    if (typeof valor !== 'string') {
      return 0;
    }
    const id: number = Number(valor);
    return Number.isInteger(id) && id > 0 ? id : 0;
  }

  /**
   * Valida el modo de transporte: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarModo(valor: unknown): string {
    if (
      typeof valor !== 'string' ||
      (valor !== 'PIE' && valor !== 'BICICLETA' && valor !== 'CARRO')
    ) {
      return 'Elige cómo quieres ir.';
    }
    return '';
  }

  /**
   * Cambia el modo de transporte y recalcula.
   *
   * @param valor Modo elegido
   */
  cambiarModo(valor: string): void {
    this.modo = valor;
    this.calcular();
  }

  /**
   * Pide la ruta al back.
   */
  calcular(): void {
    this.mensaje = this.validarModo(this.modo);
    if (this.mensaje !== '' || this.lugarId <= 0) {
      return;
    }
    if (this.ubicacion.latitud === null || this.ubicacion.longitud === null) {
      this.mensaje = 'Necesitamos tu ubicación para calcular la ruta.';
      return;
    }
    this.calculando = true;
    this.ruta = null;
    this.rutaService
      .calcular(
        this.salidaId,
        this.lugarId,
        this.ubicacion.latitud,
        this.ubicacion.longitud,
        this.modo,
      )
      .subscribe({
        next: (r: RutaModel) => {
          this.calculando = false;
          if (typeof r !== 'object' || r === null || typeof r.indicaciones !== 'string') {
            this.mensaje = 'El servidor respondió con datos no válidos.';
            return;
          }
          this.ruta = r;
          this.armarMapa(r);
        },
        error: (e: unknown) => {
          this.calculando = false;
          this.mensaje = this.mensajes.deError(e);
        },
      });
  }

  /**
   * Convierte la línea del camino al orden latitud, longitud y marca el destino.
   *
   * @param r Ruta calculada
   */
  armarMapa(r: RutaModel): void {
    const lista: number[][] = [];
    if (r.geometriaGeoJson !== null && Array.isArray(r.geometriaGeoJson.coordinates)) {
      for (const par of r.geometriaGeoJson.coordinates) {
        if (Array.isArray(par) && par.length >= 2) {
          lista.push([par[1], par[0]]);
        }
      }
    }
    this.puntos = lista;
    this.marcadores = [];
    if (lista.length > 0) {
      const fin: number[] = lista[lista.length - 1];
      this.marcadores = [
        { latitud: fin[0], longitud: fin[1], titulo: String(r.destinoNombre), destacado: true },
      ];
    }
  }
}
