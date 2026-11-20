// Sirve para /auth/forgot y /auth/resend-verification (email) y /auth/reset (token + nuevaPassword)
export interface PasswordResetModel {
  email?: string;
  token?: string;
  nuevaPassword?: string;
}
