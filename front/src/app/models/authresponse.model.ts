import { RolModel } from './rol.model';

/**
 * Respuesta del inicio de sesión.
 */
export interface AuthResponseModel {
  /** Token. */
  token: string;
  /** Rol del usuario. */
  role: RolModel;
  /** Nombre de usuario. */
  username: string;
}
