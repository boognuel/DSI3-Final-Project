/**
 * Datos para reenviar la verificación, pedir la recuperación o restablecer la contraseña.
 */
export interface PasswordResetModel {
  /** Correo electrónico. */
  email?: string;
  /** Token. */
  token?: string;
  /** Contraseña nueva. */
  nuevaPassword?: string;
}
