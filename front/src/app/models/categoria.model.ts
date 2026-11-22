/**
 * Categoría de planes.
 */
export interface CategoriaModel {
  /** Identificador. */
  id: number | null;
  /** Nombre. */
  nombre: string;
  /** Descripción. */
  descripcion: string;
  /** Etiquetas de OpenStreetMap, por ejemplo amenity=bar. */
  etiquetaOsm: string;
  /** Precio de referencia por persona. */
  precioReferencia: number | null;
}
