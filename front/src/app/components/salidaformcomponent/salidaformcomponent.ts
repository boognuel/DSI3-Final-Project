import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SalidaModel } from '../../models/salida.model';
import { FormatoService } from '../../services/formato.service';
import { MensajeService } from '../../services/mensaje.service';
import { SalidaService } from '../../services/salida.service';

/**
 * Formulario para crear o editar una salida.
 */
@Component({
  selector: 'app-salidaformcomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './salidaformcomponent.css',
  templateUrl: './salidaformcomponent.html',
})
export class Salidaformcomponent implements OnInit {
  /** Salida que se edita, o null si es nueva. */
  id: number | null = null;
  /** Título. */
  titulo: string = '';
  /** Descripción. */
  descripcion: string = '';
  /** Fecha y hora en formato aaaa-mm-ddThh:mm. */
  fechaHora: string = '';
  /** Presupuesto por persona. */
  presupuesto: number | null = null;
  /** Lugar que se propone al crear, si se llegó desde la búsqueda. */
  lugarId: number | null = null;
  /** Nombre de ese lugar. */
  lugarNombre: string = '';
  /** Salida cargada, para conservar los datos que no se editan aquí. */
  original: SalidaModel | null = null;
  /** Errores de validación por campo. */
  errores: Record<string, string> = {};
  /** Mensaje de error. */
  mensaje: string = '';
  /** Indica si hay una petición en curso. */
  enviando: boolean = false;
  /** Fecha mínima del campo de fecha. */
  minimo: string = '';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param formato Servicio de formato
   * @param route Ruta activa
   * @param router Enrutador
   * @param salidaService Servicio de salidas
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    public formato: FormatoService,
    private route: ActivatedRoute,
    private router: Router,
    private salidaService: SalidaService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Carga la salida que se edita o lee el lugar elegido en la búsqueda.
   */
  ngOnInit(): void {
    this.minimo = this.aTextoLocal(new Date(Date.now() + 60 * 60 * 1000));
    const idTexto: unknown = this.route.snapshot.paramMap.get('id');
    if (typeof idTexto === 'string') {
      const id: number = Number(idTexto);
      if (!Number.isInteger(id) || id <= 0) {
        this.mensaje = 'La salida pedida no es válida.';
        return;
      }
      this.id = id;
      this.cargar(id);
      return;
    }
    const lugar: number = Number(this.route.snapshot.queryParamMap.get('lugar'));
    const nombre: unknown = this.route.snapshot.queryParamMap.get('nombre');
    const presupuesto: number = Number(this.route.snapshot.queryParamMap.get('presupuesto'));
    if (Number.isInteger(lugar) && lugar > 0) {
      this.lugarId = lugar;
      this.lugarNombre =
        typeof nombre === 'string' && nombre.trim() !== ''
          ? nombre.trim().substring(0, 150)
          : 'el lugar elegido';
    }
    if (Number.isInteger(presupuesto) && presupuesto >= 0) {
      this.presupuesto = presupuesto;
    }
  }

  /**
   * Consulta la salida y llena el formulario.
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
        this.original = s;
        this.titulo = s.titulo;
        this.descripcion = typeof s.descripcion === 'string' ? s.descripcion : '';
        this.fechaHora = typeof s.fechaHora === 'string' ? s.fechaHora.substring(0, 16) : '';
        this.presupuesto = s.presupuesto;
      },
      error: (e: unknown) => {
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Convierte una fecha al formato del campo datetime-local.
   *
   * @param fecha Fecha
   * @returns Texto aaaa-mm-ddThh:mm
   */
  aTextoLocal(fecha: Date): string {
    const partes: number[] = [
      fecha.getMonth() + 1,
      fecha.getDate(),
      fecha.getHours(),
      fecha.getMinutes(),
    ];
    const textos: string[] = [];
    for (const p of partes) {
      textos.push(p < 10 ? '0' + p : String(p));
    }
    return (
      fecha.getFullYear() + '-' + textos[0] + '-' + textos[1] + 'T' + textos[2] + ':' + textos[3]
    );
  }

  /**
   * Valida el título: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarTitulo(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '') {
      return 'Ponle un título a la salida.';
    }
    return '';
  }

  /**
   * Valida la descripción: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarDescripcion(valor: unknown): string {
    if (typeof valor !== 'string') {
      return 'La descripción no es válida.';
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
    if (typeof valor !== 'string' || valor === '') {
      return 'Elige la fecha y la hora.';
    }
    const fecha: number = new Date(valor).getTime();
    if (Number.isNaN(fecha)) {
      return 'La fecha no es válida.';
    }
    if (this.id === null && fecha < Date.now()) {
      return 'La salida debe ser en el futuro.';
    }
    return '';
  }

  /**
   * Valida el presupuesto: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarPresupuesto(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Escribe el presupuesto por persona.';
    }
    if (valor < 0) {
      return 'El presupuesto no puede ser negativo.';
    }
    return '';
  }

  /**
   * Valida el formulario y crea o actualiza la salida.
   */
  guardar(): void {
    this.mensaje = '';
    this.errores = {
      titulo: this.validarTitulo(this.titulo),
      descripcion: this.validarDescripcion(this.descripcion),
      fechaHora: this.validarFecha(this.fechaHora),
      presupuesto: this.validarPresupuesto(this.presupuesto),
    };
    for (const campo in this.errores) {
      if (this.errores[campo] !== '') {
        return;
      }
    }
    const datos: SalidaModel = {
      id: this.id,
      titulo: this.titulo.trim(),
      descripcion: this.descripcion.trim(),
      organizadorId: null,
      organizadorNombre: null,
      presupuesto: Number(this.presupuesto),
      zonaNombre: this.original === null ? null : this.original.zonaNombre,
      latitudCentro: this.original === null ? null : this.original.latitudCentro,
      longitudCentro: this.original === null ? null : this.original.longitudCentro,
      radioMetros: this.original === null ? null : this.original.radioMetros,
      fechaHora: this.fechaHora.length === 16 ? this.fechaHora + ':00' : this.fechaHora,
      estado: this.original === null ? null : this.original.estado,
    };
    this.enviando = true;
    if (this.id !== null) {
      const id: number = this.id;
      this.salidaService.actualizar(id, datos).subscribe({
        next: () => {
          this.enviando = false;
          this.router.navigate(['/salidas', id]);
        },
        error: (e: unknown) => {
          this.enviando = false;
          this.mensaje = this.mensajes.deError(e);
        },
      });
      return;
    }
    this.salidaService.crear(datos).subscribe({
      next: (s: SalidaModel) => {
        if (typeof s !== 'object' || s === null || typeof s.id !== 'number') {
          this.enviando = false;
          this.mensaje = 'El servidor respondió con datos no válidos.';
          return;
        }
        this.proponerLugar(s.id);
      },
      error: (e: unknown) => {
        this.enviando = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }

  /**
   * Propone en la salida nueva el lugar elegido en la búsqueda y abre su detalle.
   *
   * @param salidaId Salida creada
   */
  proponerLugar(salidaId: number): void {
    if (this.lugarId === null) {
      this.enviando = false;
      this.router.navigate(['/salidas', salidaId]);
      return;
    }
    this.salidaService.agregarLugar(salidaId, this.lugarId).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/salidas', salidaId]);
      },
      error: (e: unknown) => {
        this.enviando = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
