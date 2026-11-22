/**
 * Punto que se dibuja en el mapa.
 */
export interface MarcadorModel {
  /** Latitud. */
  latitud: number;
  /** Longitud. */
  longitud: number;
  /** Título. */
  titulo: string;
  /** Indica si el punto se resalta. */
  destacado: boolean;
}
