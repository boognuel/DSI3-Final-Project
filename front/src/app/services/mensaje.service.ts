import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

/**
 * Convierte los errores de las peticiones en mensajes para el usuario, priorizando el texto que envía el back.
 */
@Injectable({
  providedIn: 'root',
})
export class MensajeService {
  /**
   * Convierte un error de una petición en un mensaje para el usuario.
   *
   * @param error Error de la petición
   * @returns Mensaje de error
   */
  deError(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'Ocurrió un error inesperado.';
    }
    if (error.status === 0) {
      return 'No hay conexión con el servidor. Revisa que el back esté encendido.';
    }
    const cuerpo: unknown = error.error;
    if (
      typeof cuerpo === 'string' &&
      cuerpo.trim() !== '' &&
      !cuerpo.trim().startsWith('{') &&
      !cuerpo.trim().startsWith('<')
    ) {
      return cuerpo;
    }
    if (error.status === 401) {
      return 'Tu sesión expiró. Inicia sesión de nuevo.';
    }
    if (error.status === 403) {
      return 'No tienes permiso para realizar esta operación.';
    }
    if (error.status === 404) {
      return 'No encontramos lo que buscabas.';
    }
    return 'Ocurrió un error inesperado (código ' + error.status + ').';
  }
}
