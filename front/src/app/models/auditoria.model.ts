/**
 * Registro de la auditoría.
 */
export interface AuditoriaModel {
  /** Identificador. */
  id: number;
  /** Fecha y hora de la acción. */
  fecha: string;
  /** Identificador del usuario, si había sesión. */
  usuarioId: number | null;
  /** Nombre del usuario. */
  usuarioNombre: string | null;
  /** Rol del usuario. */
  rolUsuario: string | null;
  /** Acción realizada. */
  accion: string;
  /** Tipo de registro afectado. */
  entidad: string | null;
  /** Identificador del registro afectado. */
  entidadId: number | null;
  /** Descripción adicional. */
  detalle: string | null;
  /** Dirección IP de origen. */
  ipOrigen: string | null;
  /** Navegador. */
  userAgent: string | null;
  /** Resultado: EXITO o FALLO. */
  resultado: string;
}
