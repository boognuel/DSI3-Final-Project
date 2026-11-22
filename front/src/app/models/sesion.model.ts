import { RolModel } from './rol.model';

/**
 * Sesión guardada en el navegador.
 */
export interface SesionModel {
  /** Token. */
  token: string;
  /** Rol del usuario. */
  role: RolModel;
  /** Nombre de usuario. */
  username: string;
}
