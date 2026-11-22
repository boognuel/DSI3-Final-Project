/**
 * Rango de precio de la portada.
 */
export interface RangoModel {
  /** Texto de la etiqueta. */
  etiqueta: string;
  /** Clase de color de la etiqueta. */
  clase: string;
  /** Título del rango. */
  titulo: string;
  /** Descripción. */
  descripcion: string;
  /** Presupuesto con el que se abre la búsqueda. */
  presupuesto: number;
}
