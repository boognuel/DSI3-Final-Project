import { Injectable } from '@angular/core';

/**
 * Da formato a pesos, fechas, distancias y duraciones para mostrarlos en pantalla.
 */
@Injectable({
  providedIn: 'root',
})
export class FormatoService {
  /**
   * Da formato de pesos colombianos.
   *
   * @param valor Valor a formatear
   * @returns Valor con signo de pesos, o Sin precio
   */
  pesos(valor: unknown): string {
    if (typeof valor !== 'number' || Number.isNaN(valor)) {
      return 'Sin precio';
    }
    return '$' + Math.round(valor).toLocaleString('es-CO');
  }

  /**
   * Da formato a una fecha.
   *
   * @param valor Valor a formatear
   * @returns Fecha legible, o vacío si no es válida
   */
  fecha(valor: unknown): string {
    if (typeof valor !== 'string' || valor === '') {
      return '';
    }
    const fecha: Date = new Date(valor);
    if (Number.isNaN(fecha.getTime())) {
      return '';
    }
    return fecha.toLocaleString('es-CO', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  /**
   * Da formato a una distancia.
   *
   * @param metros Distancia en metros
   * @returns Distancia en metros o kilómetros
   */
  distancia(metros: unknown): string {
    if (typeof metros !== 'number' || Number.isNaN(metros)) {
      return '';
    }
    if (metros >= 1000) {
      return (metros / 1000).toFixed(1).replace('.', ',') + ' km';
    }
    return Math.round(metros) + ' m';
  }

  /**
   * Da formato a una duración.
   *
   * @param segundos Duración en segundos
   * @returns Duración en minutos u horas
   */
  duracion(segundos: unknown): string {
    if (typeof segundos !== 'number' || Number.isNaN(segundos)) {
      return '';
    }
    const minutos: number = Math.round(segundos / 60);
    if (minutos < 60) {
      return minutos + ' min';
    }
    const horas: number = Math.floor(minutos / 60);
    return horas + ' h ' + (minutos - horas * 60) + ' min';
  }
}
