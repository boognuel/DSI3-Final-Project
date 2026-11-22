/**
 * Invitación a una salida.
 */
export interface InvitacionModel {
  /** Identificador. */
  id: number | null;
  /** Salida a la que se invita. */
  salidaId: number;
  /** Título de la salida. */
  salidaTitulo: string | null;
  /** Correo del invitado. */
  emailInvitado: string;
  /** Estado: PENDIENTE, ACEPTADA, RECHAZADA o EXPIRADA. */
  estado: string | null;
  /** Fecha de envío. */
  fechaEnvio: string | null;
  /** Fecha en que vence. */
  fechaExpiracion: string | null;
  /** Fecha de la respuesta. */
  fechaRespuesta: string | null;
}
