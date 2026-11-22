/**
 * Salida organizada por un usuario.
 */
export interface SalidaModel {
  /** Identificador. */
  id: number | null;
  /** Título. */
  titulo: string;
  /** Descripción. */
  descripcion: string;
  /** Organizador. */
  organizadorId: number | null;
  /** Nombre del organizador. */
  organizadorNombre: string | null;
  /** Presupuesto por persona. */
  presupuesto: number | null;
  /** Zona de referencia. */
  zonaNombre: string | null;
  /** Latitud del punto de encuentro. */
  latitudCentro: number | null;
  /** Longitud del punto de encuentro. */
  longitudCentro: number | null;
  /** Radio de búsqueda en metros. */
  radioMetros: number | null;
  /** Fecha y hora. */
  fechaHora: string;
  /** Estado: PLANEANDO, CONFIRMADA, CANCELADA o REALIZADA. */
  estado: string | null;
}
