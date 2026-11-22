import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { InvitacionService } from '../../services/invitacion.service';
import { MensajeService } from '../../services/mensaje.service';

/**
 * Página a la que lleva el enlace del correo de invitación, para aceptarla o rechazarla.
 */
@Component({
  selector: 'app-invitacioncomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './invitacioncomponent.css',
  templateUrl: './invitacioncomponent.html',
})
export class Invitacioncomponent implements OnInit {
  /** Token de la invitación. */
  token: string = '';
  /** Respuesta que eligió en el correo: si, no o vacío. */
  sugerida: string = '';
  /** Mensaje de error. */
  mensaje: string = '';
  /** Mensaje de éxito. */
  aviso: string = '';
  /** Indica si hay una petición en curso. */
  enviando: boolean = false;

  /**
   * Crea la instancia con sus dependencias.
   *
   * @param route Ruta activa
   * @param invitacionService Servicio de invitaciones
   * @param mensajes Servicio de mensajes de error
   */
  constructor(
    private route: ActivatedRoute,
    private invitacionService: InvitacionService,
    private mensajes: MensajeService,
  ) {}

  /**
   * Lee el token y la respuesta del enlace.
   */
  ngOnInit(): void {
    this.mensaje = this.validarToken(this.route.snapshot.queryParamMap.get('token'));
    if (this.mensaje !== '') {
      return;
    }
    this.token = String(this.route.snapshot.queryParamMap.get('token'));
    const respuesta: unknown = this.route.snapshot.queryParamMap.get('respuesta');
    if (respuesta === 'si' || respuesta === 'no') {
      this.sugerida = respuesta;
    }
  }

  /**
   * Valida el token del enlace: primero el tipo y después el contenido.
   *
   * @param valor Valor a validar
   * @returns Mensaje de error, o vacío si es válido
   */
  validarToken(valor: unknown): string {
    if (typeof valor !== 'string' || valor.trim() === '' || !/^[A-Za-z0-9-]+$/.test(valor)) {
      return 'El enlace de la invitación no es válido.';
    }
    return '';
  }

  /**
   * Envía la respuesta a la invitación.
   *
   * @param aceptar true para aceptar
   */
  responder(aceptar: boolean): void {
    this.mensaje = '';
    this.aviso = '';
    this.enviando = true;
    this.invitacionService.responder(this.token, aceptar).subscribe({
      next: (texto: string) => {
        this.enviando = false;
        this.aviso = texto;
      },
      error: (e: unknown) => {
        this.enviando = false;
        this.mensaje = this.mensajes.deError(e);
      },
    });
  }
}
