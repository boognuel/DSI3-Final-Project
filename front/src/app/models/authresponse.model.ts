import { RolModel } from './rol.model';

/**
 * Respuesta del inicio de sesión.
 */
export interface AuthResponseModel {
  /** Token JWT. */
  token: string;
  /** Rol del usuario. */
  role: RolModel;
}
