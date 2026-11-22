/**
 * Ruta a un lugar de una salida.
 */
export interface RutaModel {
  /** Salida. */
  salidaId: number;
  /** Lugar de destino. */
  lugarId: number;
  /** Latitud de origen. */
  origenLatitud: number;
  /** Longitud de origen. */
  origenLongitud: number;
  /** Modo: CARRO, PIE o BICICLETA. */
  modoTransporte: string;
  /** Nombre del destino. */
  destinoNombre: string | null;
  /** Distancia en metros. */
  distanciaMetros: number | null;
  /** Duración en segundos. */
  duracionSegundos: number | null;
  /** Duración en minutos. */
  duracionMinutos: number | null;
  /** Línea del camino en GeoJSON. */
  geometriaGeoJson: { type: string; coordinates: number[][] } | null;
  /** Indicaciones redactadas. */
  indicaciones: string | null;
}
