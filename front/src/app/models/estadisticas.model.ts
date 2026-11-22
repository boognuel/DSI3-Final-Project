import { ConteoModel } from './conteo.model';

/**
 * Datos de las gráficas de estadísticas.
 */
export interface EstadisticasModel {
  /** Usuarios registrados por mes. */
  usuariosPorMes: ConteoModel[];
  /** Lugares por categoría. */
  lugaresPorCategoria: ConteoModel[];
  /** Invitaciones por estado. */
  invitacionesPorEstado: ConteoModel[];
}
