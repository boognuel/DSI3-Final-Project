import { HttpErrorResponse } from '@angular/common/http';

// El backend responde los errores como texto plano
export function mensajeError(err: unknown, porDefecto = 'Ocurrió un error. Intenta de nuevo.'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'No hay conexión con el servidor.';
    if (typeof err.error === 'string' && err.error.trim()) return err.error;
    if (err.status === 400) return 'Revisa los datos ingresados.';
  }
  return porDefecto;
}
