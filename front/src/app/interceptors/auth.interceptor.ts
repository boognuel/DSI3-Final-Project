import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { SesionService } from '../services/sesion.service';
import { UbicacionService } from '../services/ubicacion.service';

@Injectable()
/**
 * Interceptor que agrega el token y la ubicación a cada petición, y cierra la sesión si el back responde 401.
 */
export class AuthInterceptor implements HttpInterceptor {
  /**
   * Crea la instancia con sus dependencias.
   *
   * @param sesion Servicio de sesión
   * @param ubicacion Servicio de ubicación
   * @param router Enrutador
   */
  constructor(
    private sesion: SesionService,
    private ubicacion: UbicacionService,
    private router: Router,
  ) {}

  /**
   * Agrega el token y la ubicación a la petición y maneja la sesión vencida.
   *
   * @param request Petición saliente
   * @param next Siguiente manejador de la cadena
   * @returns Flujo de la respuesta
   */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const encabezados: Record<string, string> = {};
    const token: string = this.sesion.token();
    if (token !== '') {
      encabezados['Authorization'] = 'Bearer ' + token;
    }
    if (this.ubicacion.latitud !== null && this.ubicacion.longitud !== null) {
      encabezados['X-Latitud'] = String(this.ubicacion.latitud);
      encabezados['X-Longitud'] = String(this.ubicacion.longitud);
    }
    const peticion: HttpRequest<unknown> = request.clone({ setHeaders: encabezados });
    return next.handle(peticion).pipe(
      catchError((error: unknown) => {
        if (
          error instanceof HttpErrorResponse &&
          error.status === 401 &&
          !request.url.includes('/auth/')
        ) {
          this.sesion.cerrar();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      }),
    );
  }
}
