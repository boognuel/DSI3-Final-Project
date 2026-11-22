import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ZonaModel } from '../../models/zona.model';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';
import { ZonaService } from '../../services/zona.service';

/**
 * Página de administración de zonas.
 */
@Component({
  selector: 'app-adminzonascomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './adminzonascomponent.css',
  templateUrl: './adminzonascomponent.html',
})
export class Adminzonascomponent implements OnInit {
  /** Zonas. */
  zonas: ZonaModel[] = [];
  /** Identificador del registro en edición, o null si se está creando uno. */
  editando: number | null = null;
  /** Nombre. */
  nombre: string = '';
  /** Descripción. */
  descripcion: string = '';
  /** Gasto mínimo por persona. */
  gastoMinimo: number | null = null;
  /** Gasto máximo por persona. */
  gastoMaximo: number | null = null;
  /** Latitud del borde sur. */
  latitudMinima: number | null = null;
  /** Latitud del borde norte. */
  latitudMaxima: number | null = null;
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
   * @param zonaService Servicio de zonas
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private zonaService: ZonaService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga las zonas al abrir la página.
   */
  ngOnInit(): void {
    this.cargar();
  }

  /**
   * Consulta las zonas.
   */
  cargar(): void {
    this.zonaService.getAll().subscribe({
      next: (lista: ZonaModel[] | null) => {
        this.zonas = Array.isArray(lista) ? lista : [];
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Limpia el formulario para crear una zona.
   */
  nuevo(): void {
    this.editando = null;
    this.nombre = '';
    this.descripcion = '';
    this.gastoMinimo = null;
    this.gastoMaximo = null;
    this.latitudMinima = null;
    this.latitudMaxima = null;
    this.errores = {};
  }

  /**
   * Llena el formulario con una zona para editarla.
   *
   * @param z Zona
   */
  editar(z: ZonaModel): void {
    this.editando = z.id;
    this.nombre = z.nombre;
    this.descripcion = typeof z.descripcion === 'string' ? z.descripcion : '';
    this.gastoMinimo = z.gastoMinimo;
    this.gastoMaximo = z.gastoMaximo;
    this.latitudMinima = z.latitudMinima;
    this.latitudMaxima = z.latitudMaxima;
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
    if (valor.trim().length > 80) {
      return 'El nombre admite hasta 80 caracteres.';
    }
    return '';
  }

  /**
   * Valida un gasto: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarGasto(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe el valor en pesos.';
    }
    if (valor < 0) {
      return 'No puede ser negativo.';
    }
    return '';
  }

  /**
   * Valida una latitud, que es opcional: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarLatitud(valor: unknown): string {
    if (valor === null) {
      return '';
    }
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe la latitud con punto decimal, por ejemplo 4.705.';
    }
    if (valor < 4.655 || valor > 4.84) {
      return 'Debe estar entre 4.655 y 4.840.';
    }
    return '';
  }

  /**
   * Valida el formulario y crea o actualiza la zona.
   */
  guardar(): void {
    this.mensaje = '';
    this.aviso = '';
    this.errores = {
      nombre: this.validarNombre(this.nombre),
      gastoMinimo: this.validarGasto(this.gastoMinimo),
      gastoMaximo: this.validarGasto(this.gastoMaximo),
      latitudMinima: this.validarLatitud(this.latitudMinima),
      latitudMaxima: this.validarLatitud(this.latitudMaxima),
    };
    if (this.errores['gastoMaximo'] === '' && Number(this.gastoMinimo) > Number(this.gastoMaximo)) {
      this.errores['gastoMaximo'] = 'Debe ser mayor o igual que el gasto mínimo.';
    }
    if ((this.latitudMinima === null) !== (this.latitudMaxima === null)) {
      this.errores['latitudMaxima'] = 'Escribe las dos latitudes o ninguna.';
    } else if (
      this.latitudMinima !== null &&
      this.errores['latitudMaxima'] === '' &&
      Number(this.latitudMinima) >= Number(this.latitudMaxima)
    ) {
      this.errores['latitudMaxima'] = 'Debe ser mayor que la latitud mínima.';
    }
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    const datos: ZonaModel = {
      id: this.editando,
      nombre: this.nombre.trim(),
      descripcion: this.descripcion.trim(),
      gastoMinimo: Number(this.gastoMinimo),
      gastoMaximo: Number(this.gastoMaximo),
      latitudMinima: this.latitudMinima,
      latitudMaxima: this.latitudMaxima,
    };
    const peticion =
      this.editando === null
        ? this.zonaService.crear(datos)
        : this.zonaService.actualizar(this.editando, datos);
    peticion.subscribe({
      next: () => {
        this.aviso = this.editando === null ? 'Zona creada.' : 'Zona actualizada.';
        this.nuevo();
        this.cargar();
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Elimina una zona que no tenga lugares.
   *
   * @param z Zona
   */
  eliminar(z: ZonaModel): void {
    this.mensaje = '';
    this.aviso = '';
    if (z.id === null) {
      return;
    }
    this.zonaService.eliminar(z.id).subscribe({
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
