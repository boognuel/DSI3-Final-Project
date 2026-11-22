/**
 * Usuario de la aplicación.
 */
export interface UsuarioModel {
  /** Identificador. */
  id: number | null;
  /** Nombre de usuario. */
  username: string;
  /** Correo. */
  email: string;
  /** Contraseña, solo al crear o cambiarla. */
  password: string | null;
  /** Rol: USER o ADMIN. */
  role: string;
  /** Indica si la cuenta está habilitada. */
  enabled: boolean;
  /** Indica si el correo está verificado. */
  emailVerificado: boolean;
}
