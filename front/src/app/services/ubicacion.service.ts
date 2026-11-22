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

  /**
   * Indica si la ubicación está fuera de la zona que cubre la app, de la calle 75 a la calle 222.
   * Usa los mismos límites que el back.
   *
   * @returns true si hay ubicación y está fuera de la zona
   */
  fueraDeZona(): boolean {
    if (this.latitud === null || this.longitud === null) {
      return false;
    }
    return (
      this.latitud < 4.655 ||
      this.latitud > 4.84 ||
      this.longitud < -74.13 ||
      this.longitud > -73.99
    );
  }

  /**
   * Calcula la distancia en línea recta desde la ubicación del usuario hasta un punto.
   *
   * @param latitud Latitud del punto
   * @param longitud Longitud del punto
   * @returns Distancia en metros, o null si no hay ubicación
   */
  distanciaA(latitud: number, longitud: number): number | null {
    if (this.latitud === null || this.longitud === null) {
      return null;
    }
    const radio: number = 6371000;
    const dLat: number = ((latitud - this.latitud) * Math.PI) / 180;
    const dLon: number = ((longitud - this.longitud) * Math.PI) / 180;
    const a: number =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((this.latitud * Math.PI) / 180) *
        Math.cos((latitud * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    return Math.round(radio * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
  }
}
