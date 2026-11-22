import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuditoriaModel } from '../models/auditoria.model';
import { EstadisticasModel } from '../models/estadisticas.model';
import { PaginaModel } from '../models/pagina.model';

/**
 * Peticiones del panel de administración: estadísticas, auditoría y reporte.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminService {
  /** Dirección base de las peticiones. */
  private url: string = environment.apiUrl + '/admin';

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param http Cliente HTTP
   */
  constructor(private http: HttpClient) {}

  /**
   * Consulta las series de las gráficas.
   *
   * @returns Estadísticas
   */
  estadisticas(): Observable<EstadisticasModel> {
    return this.http.get<EstadisticasModel>(this.url + '/estadisticas');
  }

  /**
   * Busca registros de la auditoría.
   *
   * @param usuario Parte del nombre de usuario, o vacío
   * @param accion Acción, o vacío para todas
   * @param desde Fecha inicial aaaa-mm-dd, o vacío
   * @param hasta Fecha final aaaa-mm-dd, o vacío
   * @param pagina Página desde 0
   * @returns Página de registros
   */
  auditoria(
    usuario: string,
    accion: string,
    desde: string,
    hasta: string,
    pagina: number,
  ): Observable<PaginaModel<AuditoriaModel>> {
    let params: HttpParams = new HttpParams().set('page', String(pagina)).set('size', '20');
    if (usuario !== '') {
      params = params.set('usuario', usuario);
    }
    if (accion !== '') {
      params = params.set('accion', accion);
    }
    if (desde !== '') {
      params = params.set('desde', desde);
    }
    if (hasta !== '') {
      params = params.set('hasta', hasta);
    }
    return this.http.get<PaginaModel<AuditoriaModel>>(this.url + '/auditoria', { params: params });
  }

  /**
   * Descarga el reporte en Excel.
   *
   * @param desde Fecha inicial aaaa-mm-dd, o vacío
   * @param hasta Fecha final aaaa-mm-dd, o vacío
   * @returns Archivo
   */
  reporte(desde: string, hasta: string): Observable<Blob> {
    let params: HttpParams = new HttpParams();
    if (desde !== '') {
      params = params.set('desde', desde);
    }
    if (hasta !== '') {
      params = params.set('hasta', hasta);
    }
    return this.http.get(this.url + '/reporte', { params: params, responseType: 'blob' });
  }
}
