import { Injectable, signal } from '@angular/core';
import { CoordenadasModel } from '../models/coordenadas.model';
import { EstadoUbicacionModel } from '../models/estado-ubicacion.model';

const CLAVE = 'bogoparche.ubicacion';

@Injectable({
  providedIn: 'root',
})
export class Locationservice {
  private estadoActual = signal<EstadoUbicacionModel>(this.leerEstado());
  private coordenadas = signal<CoordenadasModel | null>(null);

  readonly estado = this.estadoActual.asReadonly();
  readonly coords = this.coordenadas.asReadonly();

  constructor() {
    if (this.estadoActual() === 'concedida') {
      this.obtener();
    }
  }

  solicitar(): void {
    this.obtener();
  }

  rechazar(): void {
    this.cambiarEstado('rechazada');
  }

  private obtener(): void {
    if (typeof navigator === 'undefined' || !('geolocation' in navigator)) {
      this.cambiarEstado('rechazada');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.coordenadas.set({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        this.cambiarEstado('concedida');
      },
      () => this.cambiarEstado('rechazada'),
      { timeout: 10000 },
    );
  }

  private cambiarEstado(estado: EstadoUbicacionModel): void {
    try {
      localStorage.setItem(CLAVE, estado);
    } catch {
      /* almacenamiento no disponible */
    }
    this.estadoActual.set(estado);
  }

  private leerEstado(): EstadoUbicacionModel {
    try {
      const valor = localStorage.getItem(CLAVE);
      return valor === 'concedida' || valor === 'rechazada' ? valor : 'pendiente';
    } catch {
      return 'pendiente';
    }
  }
}
