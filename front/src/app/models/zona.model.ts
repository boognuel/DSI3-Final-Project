/**
 * Zona de la ciudad por franjas de calles.
 */
export interface ZonaModel {
  /** Identificador. */
  id: number | null;
  /** Nombre. */
  nombre: string;
  /** Descripción. */
  descripcion: string;
  /** Gasto mínimo por persona. */
  gastoMinimo: number | null;
  /** Gasto máximo por persona. */
  gastoMaximo: number | null;
  /** Latitud del borde sur. */
  latitudMinima: number | null;
  /** Latitud del borde norte. */
  latitudMaxima: number | null;
}
