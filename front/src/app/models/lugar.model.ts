/**
 * Lugar del catálogo.
 */
export interface LugarModel {
  /** Identificador. */
  id: number | null;
  /** Nombre. */
  nombre: string;
  /** Descripción. */
  descripcion: string;
  /** Categoría. */
  categoriaId: number | null;
  /** Nombre de la categoría. */
  categoriaNombre: string | null;
  /** Zona. */
  zonaId: number | null;
  /** Nombre de la zona. */
  zonaNombre: string | null;
  /** Dirección. */
  direccion: string;
  /** Latitud. */
  latitud: number | null;
  /** Longitud. */
  longitud: number | null;
  /** Gasto por persona. */
  costoEstimado: number | null;
  /** Indica si el gasto es la referencia de la categoría. */
  precioEstimado: boolean;
}
