import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { CategoriaModel } from '../../models/categoria.model';
import { LugarModel } from '../../models/lugar.model';
import { ZonaModel } from '../../models/zona.model';
import { CategoriaService } from '../../services/categoria.service';
import { FormatoService } from '../../services/formato.service';
import { LugarService } from '../../services/lugar.service';
import { MensajeService } from '../../services/mensaje.service';
import { ZonaService } from '../../services/zona.service';

/**
 * Página de administración de lugares y carga desde OpenStreetMap.
 */
@Component({
  selector: 'app-adminlugarescomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminlugarescomponent.css',
  templateUrl: './adminlugarescomponent.html',
})
export class Adminlugarescomponent implements OnInit {
  /** Lugares. */
  lugares: LugarModel[] = [];
  /** Categorías. */
  categorias: CategoriaModel[] = [];
  /** Zonas. */
  zonas: ZonaModel[] = [];
  /** Texto para filtrar por nombre. */
  filtroNombre: string = '';
  /** Categoría para filtrar. */
  filtroCategoria: number | null = null;
  /** Zona para filtrar. */
  filtroZona: number | null = null;
  /** Identificador del registro en edición, o null si se está creando uno. */
  editando: number | null = null;
  /** Nombre. */
  nombre: string = '';
  /** Descripción. */
  descripcion: string = '';
  /** Dirección. */
  direccion: string = '';
  /** Latitud. */
  latitud: number | null = null;
  /** Longitud. */
  longitud: number | null = null;
  /** Categoría. */
  categoriaId: number | null = null;
  /** Zona. */
  zonaId: number | null = null;
  /** Gasto por persona. */
  costoEstimado: number | null = null;
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';
  /** Indica si hay una carga desde OpenStreetMap en curso. */
  cargandoOsm: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param lugarService Servicio de lugares
   * @param categoriaService Servicio de categorías
   * @param zonaService Servicio de zonas
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private lugarService: LugarService,
    private categoriaService: CategoriaService,
    private zonaService: ZonaService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga las categorías, las zonas y los lugares.
   */
  ngOnInit(): void {
    this.categoriaService.getAll().subscribe({
      next: (lista: CategoriaModel[] | null) => {
        this.categorias = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
    this.zonaService.getAll().subscribe({
      next: (lista: ZonaModel[] | null) => {
        this.zonas = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
    this.buscar();
  }

  /**
   * Consulta los lugares con los filtros de zona y categoría.
   */
  buscar(): void {
    const zona: number | null = this.filtroZona === null ? null : Number(this.filtroZona);
    const categoria: number | null =
      this.filtroCategoria === null ? null : Number(this.filtroCategoria);
    this.lugarService.getAll(zona, categoria).subscribe({
      next: (lista: LugarModel[]) => {
        this.lugares = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Filtra la lista por el nombre escrito.
   *
   * @returns Lugares que coinciden
   */
  filtrados(): LugarModel[] {
    const texto: string = this.filtroNombre.trim().toLowerCase();
    if (texto === '') {
      return this.lugares;
    }
    const lista: LugarModel[] = [];
    for (const l of this.lugares) {
      if (l.nombre.toLowerCase().includes(texto)) {
        lista.push(l);
      }
    }
    return lista;
  }

  /**
   * Carga o actualiza los lugares desde OpenStreetMap.
   */
  cargarOsm(): void {
    this.mensaje = '';
    this.aviso = '';
    this.cargandoOsm = true;
    this.lugarService.cargarOsm().subscribe({
      next: (texto: string) => {
        this.cargandoOsm = false;
        this.aviso = texto;
        this.buscar();
      },
      error: (e: unknown) => {
        this.cargandoOsm = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Limpia el formulario para crear un lugar.
   */
  nuevo(): void {
    this.editando = null;
    this.nombre = '';
    this.descripcion = '';
    this.direccion = '';
    this.latitud = null;
    this.longitud = null;
    this.categoriaId = null;
    this.zonaId = null;
    this.costoEstimado = null;
    this.errores = {};
  }

  /**
   * Llena el formulario con un lugar para editarlo.
   *
   * @param l Lugar
   */
  editar(l: LugarModel): void {
    this.editando = l.id;
    this.nombre = l.nombre;
    this.descripcion = typeof l.descripcion === 'string' ? l.descripcion : '';
    this.direccion = l.direccion;
    this.latitud = l.latitud;
    this.longitud = l.longitud;
    this.categoriaId = l.categoriaId;
    this.zonaId = l.zonaId;
    this.costoEstimado = l.costoEstimado;
    this.errores = {};
  }

  /**
   * Valida un texto obligatorio: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @param campo Nombre del campo para el mensaje
   * @returns Mensaje de error, o vacío si es válido
   */
  validarTexto(valor: unknown, campo: string): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return campo + ' es obligatorio.';
    }
    if (valor.trim().length > 255) {
      return campo + ' admite hasta 255 caracteres.';
    }
    return '';
  }

  /**
   * Valida la latitud: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarLatitud(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe la latitud con punto decimal.';
    }
    if (valor < 4.655 || valor > 4.84) {
      return 'Debe estar entre 4.655 y 4.840, de la calle 75 al norte.';
    }
    return '';
  }

  /**
   * Valida la longitud: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarLongitud(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe la longitud con punto decimal.';
    }
    if (valor < -74.13 || valor > -73.99) {
      return 'Debe estar entre -74.130 y -73.990.';
    }
    return '';
  }

  /**
   * Valida una lista desplegable: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarSeleccion(valor: unknown): string {
    if (typeof valor !== 'number' || !Number.isInteger(valor) || valor <= 0) {
      return 'Elige una opción.';
    }
    return '';
  }

  /**
   * Valida el gasto por persona: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarCosto(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe el gasto por persona en pesos.';
    }
    if (valor < 0) {
      return 'No puede ser negativo.';
    }
    return '';
  }

  /**
   * Valida el formulario y crea o actualiza el lugar.
   */
  guardar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errores = {
      nombre: this.validarTexto(this.nombre, 'El nombre'),
      direccion: this.validarTexto(this.direccion, 'La dirección'),
      latitud: this.validarLatitud(this.latitud),
      longitud: this.validarLongitud(this.longitud),
      categoriaId: this.validarSeleccion(
        this.categoriaId === null ? null : Number(this.categoriaId),
      ),
      zonaId: this.validarSeleccion(this.zonaId === null ? null : Number(this.zonaId)),
      costoEstimado: this.validarCosto(this.costoEstimado),
    };
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    const datos: LugarModel = {
      id: this.editando,
      nombre: this.nombre.trim(),
      descripcion: this.descripcion.trim(),
      categoriaId: Number(this.categoriaId),
      categoriaNombre: null,
      zonaId: Number(this.zonaId),
      zonaNombre: null,
      direccion: this.direccion.trim(),
      latitud: this.latitud,
      longitud: this.longitud,
      costoEstimado: this.costoEstimado,
      precioEstimado: false,
    };
    if (this.editando === null) {
      this.lugarService.crear(datos).subscribe({
        next: () => {
          this.terminarGuardado('Lugar creado.');
        },
        error: (e: unknown) => {
          this.mensaje = this.mensajes.deError(e);
        },
      });
      return;
    }
    this.lugarService.actualizar(this.editando, datos).subscribe({
      next: () => {
        this.terminarGuardado('Lugar actualizado.');
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Muestra el aviso, limpia el formulario y recarga la lista.
   *
   * @param texto Aviso para el usuario
   */
  terminarGuardado(texto: string): void {
    this.aviso = texto;
    this.nuevo();
    this.buscar();
  }

  /**
   * Elimina un lugar que no esté en salidas ni rutas.
   *
   * @param l Lugar
   */
  eliminar(l: LugarModel): void {
    this.mensaje = '';
    this.aviso = '';
    if (l.id === null) {
      return;
    }
    this.lugarService.eliminar(l.id).subscribe({
      next: (texto: string) => {
        this.aviso = texto;
        this.buscar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
