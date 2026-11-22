import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { CategoriaModel } from '../../models/categoria.model';
import { CategoriaService } from '../../services/categoria.service';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';

/**
 * Página de administración de categorías.
 */
@Component({
  selector: 'app-admincategoriascomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './admincategoriascomponent.css',
  templateUrl: './admincategoriascomponent.html',
})
export class Admincategoriascomponent implements OnInit {
  /** Categorías. */
  categorias: CategoriaModel[] = [];
  /** Identificador del registro en edición, o null si se está creando uno. */
  editando: number | null = null;
  /** Nombre. */
  nombre: string = '';
  /** Descripción. */
  descripcion: string = '';
  /** Etiquetas de OpenStreetMap. */
  etiquetaOsm: string = '';
  /** Precio de referencia por persona. */
  precioReferencia: number | null = null;
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param categoriaService Servicio de categorías
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private categoriaService: CategoriaService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga las categorías al abrir la página.
   */
  ngOnInit(): void {
    this.cargar();
  }

  /**
   * Consulta las categorías.
   */
  cargar(): void {
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
   * Limpia el formulario para crear una categoría.
   */
  nuevo(): void {
    this.editando = null;
    this.nombre = '';
    this.descripcion = '';
    this.etiquetaOsm = '';
    this.precioReferencia = null;
    this.errores = {};
  }

  /**
   * Llena el formulario con una categoría para editarla.
   *
   * @param c Categoría
   */
  editar(c: CategoriaModel): void {
    this.editando = c.id;
    this.nombre = c.nombre;
    this.descripcion = typeof c.descripcion === 'string' ? c.descripcion : '';
    this.etiquetaOsm = typeof c.etiquetaOsm === 'string' ? c.etiquetaOsm : '';
    this.precioReferencia = c.precioReferencia;
    this.errores = {};
  }

  /**
   * Valida el nombre: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarNombre(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'El nombre es obligatorio.';
    }
    if (valor.trim().length > 50) {
      return 'El nombre admite hasta 50 caracteres.';
    }
    return '';
  }

  /**
   * Valida las etiquetas de OpenStreetMap, que son opcionales: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarEtiquetas(valor: unknown): string {
    if (typeof valor !== 'string') {
      return 'Las etiquetas no son válidas.';
    }
    if (valor.trim() !== '' && !/^[a-z_]+=[a-z_]+(,[a-z_]+=[a-z_]+)*$/.test(valor.trim())) {
      return 'Usa la forma clave=valor separada por comas, por ejemplo amenity=bar,amenity=pub.';
    }
    return '';
  }

  /**
   * Valida el precio de referencia, que es opcional: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarPrecio(valor: unknown): string {
    if (valor === null) {
      return '';
    }
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe el precio en pesos.';
    }
    if (valor < 0) {
      return 'No puede ser negativo.';
    }
    return '';
  }

  /**
   * Valida el formulario y crea o actualiza la categoría.
   */
  guardar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errores = {
      nombre: this.validarNombre(this.nombre),
      etiquetaOsm: this.validarEtiquetas(this.etiquetaOsm),
      precioReferencia: this.validarPrecio(this.precioReferencia),
    };
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    const datos: CategoriaModel = {
      id: this.editando,
      nombre: this.nombre.trim(),
      descripcion: this.descripcion.trim(),
      etiquetaOsm: this.etiquetaOsm.trim(),
      precioReferencia: this.precioReferencia,
    };
    const peticion =
      this.editando === null
        ? this.categoriaService.crear(datos)
        : this.categoriaService.actualizar(this.editando, datos);
    peticion.subscribe({
      next: () => {
        this.aviso = this.editando === null ? 'Categoría creada.' : 'Categoría actualizada.';
        this.nuevo();
        this.cargar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Elimina una categoría que no tenga lugares.
   *
   * @param c Categoría
   */
  eliminar(c: CategoriaModel): void {
    this.mensaje = '';
    this.aviso = '';
    if (c.id === null) {
      return;
    }
    this.categoriaService.eliminar(c.id).subscribe({
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
