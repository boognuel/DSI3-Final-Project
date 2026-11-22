import { LugarModel } from './lugar.model';
import { ZonaModel } from './zona.model';

/**
 * Resultado de la búsqueda de planes.
 */
export interface RecomendacionModel {
  /** Presupuesto consultado. */
  presupuesto: number;
  /** Zonas a las que alcanza. */
  zonas: ZonaModel[];
  /** Lugares que caben, del más económico al más costoso. */
  lugares: LugarModel[];
}
