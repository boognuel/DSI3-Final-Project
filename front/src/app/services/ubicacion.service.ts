import { Injectable } from '@angular/core';

/**
 * Obtiene la ubicación del usuario con el navegador y recuerda si ya vio el aviso.
 */
@Injectable({
  providedIn: 'root',
})
export class UbicacionService {
  /** Latitud del usuario, o null si no se ha obtenido. */
  latitud: number | null = null;
  /** Longitud del usuario, o null si no se ha obtenido. */
  longitud: number | null = null;
  /** Clave del almacenamiento donde se recuerda el aviso. */
  private claveAviso: string = 'bp_aviso_ubicacion';

  /**
   * Indica si el usuario ya vio el aviso de ubicación.
   *
   * @returns true si ya lo vio
   */
  avisoVisto(): boolean {
    try {
      return localStorage.getItem(this.claveAviso) === 'si';
    } catch {
      return false;
    }
  }

  /**
   * Recuerda que el usuario ya vio el aviso de ubicación.
   *
   * @returns true si se pudo recordar
   */
  marcarAviso(): boolean {
    try {
      localStorage.setItem(this.claveAviso, 'si');
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Indica si ya se obtuvo la ubicación.
   *
   * @returns true si hay latitud y longitud
   */
  tieneUbicacion(): boolean {
    return typeof this.latitud === 'number' && typeof this.longitud === 'number';
  }

  /**
   * Pide la ubicación al navegador.
   *
   * @returns Promesa con un mensaje de error, o vacío si se obtuvo
   */
  obtener(): Promise<string> {
    return new Promise<string>((resolve) => {
      if (typeof navigator === 'undefined' || !navigator.geolocation) {
        resolve('Tu navegador no permite compartir la ubicación.');
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (posicion: GeolocationPosition) => {
          const lat: unknown = posicion.coords.latitude;
          const lon: unknown = posicion.coords.longitude;
          if (
            typeof lat !== 'number' ||
            typeof lon !== 'number' ||
            Number.isNaN(lat) ||
            Number.isNaN(lon)
          ) {
            resolve('El navegador entregó una ubicación no válida.');
            return;
          }
          this.latitud = lat;
          this.longitud = lon;
          resolve('');
        },
        (error: GeolocationPositionError) => {
          if (error.code === error.PERMISSION_DENIED) {
            resolve(
              'No diste permiso de ubicación. Actívalo en la configuración del navegador para buscar planes.',
            );
          } else if (error.code === error.TIMEOUT) {
            resolve('La ubicación tardó demasiado. Intenta de nuevo.');
          } else {
            resolve('No pudimos obtener tu ubicación. Intenta de nuevo.');
          }
        },
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 60000 },
      );
    });
  }
}
