import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoriaModel } from '../../models/categoria.model';
import { LugarModel } from '../../models/lugar.model';
import { MarcadorModel } from '../../models/marcador.model';
import { RecomendacionModel } from '../../models/recomendacion.model';
import { CategoriaService } from '../../services/categoria.service';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';
import { RecomendacionService } from '../../services/recomendacion.service';
import { SalidaService } from '../../services/salida.service';
import { SesionService } from '../../services/sesion.service';
import { UbicacionService } from '../../services/ubicacion.service';

/**
 * Búsqueda de planes por presupuesto, con la lista de lugares y el mapa.
 */
@Component({
  selector: 'app-buscadorcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './buscadorcomponent.css',
  templateUrl: './buscadorcomponent.html',
})
export class Buscadorcomponent implements OnInit {
  /** Presupuesto por persona. */
  presupuesto: number | null = null;
  /** Orden de la lista: PRECIO o DISTANCIA. */
  orden: string = 'PRECIO';
  /** Categorías elegidas. */
  seleccionadas: number[] = [];
  /** Categorías disponibles. */
  categorias: CategoriaModel[] = [];
  /** Resultado de la búsqueda. */
  respuesta: RecomendacionModel | null = null;
  /** Lugares en el orden elegido. */
  lugares: LugarModel[] = [];
  /** Marcadores del mapa. */
  marcadores: MarcadorModel[] = [];
  /** Lugar resaltado en el mapa. */
  elegido: number | null = null;
  /** Error del presupuesto. */
  errorPresupuesto: string = '';
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje sobre la ubicación. */
  mensajeUbicacion: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';
  /** Indica si hay una búsqueda en curso. */
  buscando: boolean = false;
  /** Presupuestos sugeridos. */
  sugeridos: number[] = [15000, 30000, 50000, 80000];
  /** Salida a la que se agregan lugares, si se llegó desde una salida. */
  salidaId: number | null = null;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param ubicacion Servicio de ubicación
   * @param formato Servicio de formato
   * @param sesion Servicio de sesión
   * @param categoriaService Servicio de categorías
   * @param recomendacionService Servicio de búsqueda
   * @param salidaService Servicio de salidas
   * @param mensajes Servicio de mensajes de error
   * @param route Ruta activa
   * @param router Enrutador
   */
  constructor(
    public ubicacion: UbicacionService,
    public formato: FormatoService,
    public sesion: SesionService,
    private categoriaService: CategoriaService,
    private recomendacionService: RecomendacionService,
    private salidaService: SalidaService,
    private mensajes: MensajeService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  /**
   * Lee los parámetros de la URL, pide la ubicación y carga las categorías.
   */
  ngOnInit(): void {
    const salida: number = Number(this.route.snapshot.queryParamMap.get('salida'));
    if (Number.isInteger(salida) && salida > 0) {
      this.salidaId = salida;
    }
    const valor: string | null = this.route.snapshot.queryParamMap.get('presupuesto');
    if (typeof valor === 'string' && valor !== '') {
      const numero: number = Number(valor);
      if (this.validarPresupuesto(numero) === '') {
        this.presupuesto = numero;
      }
    }
    if (!this.ubicacion.tieneUbicacion()) {
      this.actualizarUbicacion();
    }
    this.categoriaService.getAll().subscribe({
      next: (lista: CategoriaModel[] | null) => {
        this.categorias = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Pide de nuevo la ubicación del navegador.
   */
  actualizarUbicacion(): void {
    this.mensajeUbicacion = '';
    this.ubicacion.obtener().then((error: string) => {
      this.mensajeUbicacion = error;
      this.ordenar();
    });
  }

  /**
   * Marca o desmarca una categoría.
   *
   * @param id Categoría
   */
  alternar(id: number | null): void {
    if (id === null) {
      return;
    }
    const posicion: number = this.seleccionadas.indexOf(id);
    if (posicion >= 0) {
      this.seleccionadas.splice(posicion, 1);
    } else {
      this.seleccionadas.push(id);
    }
  }

  /**
   * Indica si una categoría está elegida.
   *
   * @param id Categoría
   * @returns true si está elegida
   */
  estaElegida(id: number | null): boolean {
    return id !== null && this.seleccionadas.indexOf(id) >= 0;
  }

  /**
   * Usa un presupuesto sugerido.
   *
   * @param valor Presupuesto
   */
  usarSugerido(valor: number): void {
    this.presupuesto = valor;
    this.errorPresupuesto = '';
  }

  /**
   * Valida el presupuesto: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarPresupuesto(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe cuánto tienes para salir, en pesos.';
    }
    if (!Number.isInteger(valor)) {
      return 'Escribe el valor sin decimales.';
    }
    if (valor < 0) {
      return 'El presupuesto no puede ser negativo.';
    }
    return '';
  }

  /**
   * Valida el formulario y busca los planes.
   */
  buscar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errorPresupuesto = this.validarPresupuesto(this.presupuesto);
    if (this.errorPresupuesto !== '') {
      return;
    }
    this.buscando = true;
    this.recomendacionService.buscar(Number(this.presupuesto), this.seleccionadas).subscribe({
      next: (r: RecomendacionModel) => {
        this.buscando = false;
        if (typeof r !== 'object' || r === null || !Array.isArray(r.lugares)) {
          this.mensaje = 'El servidor respondió con datos no válidos.';
          return;
        }
        this.respuesta = r;
        this.elegido = null;
        this.ordenar();
      },
      error: (e: unknown) => {
        this.buscando = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Distancia desde el usuario hasta un lugar.
   *
   * @param l Lugar
   * @returns Distancia en metros, o null si no se conoce
   */
  distancia(l: LugarModel): number | null {
    if (l.latitud === null || l.longitud === null) {
      return null;
    }
    return this.ubicacion.distanciaA(l.latitud, l.longitud);
  }

  /**
   * Ordena los lugares por precio, como llegan del back, o por distancia.
   */
  ordenar(): void {
    const lista: LugarModel[] = [];
    if (this.respuesta !== null) {
      for (const l of this.respuesta.lugares) {
        lista.push(l);
      }
    }
    if (this.orden === 'DISTANCIA' && this.ubicacion.tieneUbicacion()) {
      lista.sort((a: LugarModel, b: LugarModel) => {
        const da: number | null = this.distancia(a);
        const db: number | null = this.distancia(b);
        return (da === null ? Number.MAX_VALUE : da) - (db === null ? Number.MAX_VALUE : db);
      });
    }
    this.lugares = lista;
    this.armarMarcadores();
  }

  /**
   * Resalta un lugar en el mapa.
   *
   * @param id Lugar
   */
  elegir(id: number | null): void {
    this.elegido = id;
    this.armarMarcadores();
  }

  /**
   * Arma los marcadores del mapa con los lugares encontrados.
   */
  armarMarcadores(): void {
    const lista: MarcadorModel[] = [];
    for (const l of this.lugares) {
      if (l.latitud !== null && l.longitud !== null) {
        lista.push({
          latitud: l.latitud,
          longitud: l.longitud,
          titulo: l.nombre + ' - ' + this.formato.pesos(l.costoEstimado),
          destacado: l.id === this.elegido,
        });
      }
    }
    this.marcadores = lista;
  }

  /**
   * Abre el formulario de una salida nueva con este lugar propuesto.
   *
   * @param l Lugar
   */
  armarSalida(l: LugarModel): void {
    this.router.navigate(['/salidas/nueva'], {
      queryParams: { lugar: l.id, nombre: l.nombre, presupuesto: this.presupuesto },
    });
  }

  /**
   * Propone el lugar en la salida desde la que se llegó.
   *
   * @param l Lugar
   */
  agregarASalida(l: LugarModel): void {
    this.mensaje = '';
    this.aviso = '';
    if (this.salidaId === null || l.id === null) {
      return;
    }
    this.salidaService.agregarLugar(this.salidaId, l.id).subscribe({
      next: (texto: string) => {
        this.aviso = texto;
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
