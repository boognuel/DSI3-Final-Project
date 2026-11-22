import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RutaModel } from '../models/ruta.model';

/**
 * Peticiones de rutas.
 */
@Injectable({
  providedIn: 'root',
})
export class RutaService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/ruta';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Calcula la ruta desde la ubicación del usuario hasta un lugar de una salida.
   *
   * @param salidaId Salida
   * @param lugarId Lugar de destino
   * @param latitud Latitud de origen
   * @param longitud Longitud de origen
   * @param modo CARRO, PIE o BICICLETA
   * @returns Ruta calculada con sus indicaciones
   */
  calcular(
    salidaId: number,
    lugarId: number,
    latitud: number,
    longitud: number,
    modo: string,
  ): Observable<RutaModel> {
    return this.http.post<RutaModel>(this.url, {
      salidaId: salidaId,
      lugarId: lugarId,
      origenLatitud: latitud,
      origenLongitud: longitud,
      modoTransporte: modo,
    });
  }
}
