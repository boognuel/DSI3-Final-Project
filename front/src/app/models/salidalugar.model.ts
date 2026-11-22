/**
 * Lugar propuesto en una salida.
 */
export interface SalidaLugarModel {
  /** Identificador. */
  id: number;
  /** Salida. */
  salidaId: number;
  /** Lugar. */
  lugarId: number;
  /** Nombre del lugar. */
  lugarNombre: string;
  /** Categoría del lugar. */
  categoria: string | null;
  /** Latitud. */
  latitud: number;
  /** Longitud. */
  longitud: number;
  /** Orden en que se propuso. */
  orden: number;
  /** Indica si es el destino elegido. */
  seleccionado: boolean;
  /** Gasto por persona. */
  costoEstimadoPorPersona: number | null;
}
