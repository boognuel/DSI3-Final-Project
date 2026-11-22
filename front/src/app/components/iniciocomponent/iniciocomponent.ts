import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PasoModel } from '../../models/paso.model';
import { PlanModel } from '../../models/plan.model';
import { RangoModel } from '../../models/rango.model';

/**
 * Portada: presentación, rangos de precio, ideas de planes y cómo funciona.
 */
@Component({
  selector: 'app-iniciocomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './iniciocomponent.css',
  templateUrl: './iniciocomponent.html',
})
export class Iniciocomponent {
  /** Plan destacado. */
  destacado: PlanModel = {
    titulo: 'Picnic en el parque',
    detalle: 'Por persona · Tarde · Aire libre',
    precio: '$25.000',
    tono: 'tono-1',
  };

  /** Rangos de precio con el presupuesto que se busca al elegirlos. */
  rangos: RangoModel[] = [
    {
      etiqueta: '$ Económico',
      clase: 'badge-verde',
      titulo: 'Hasta $30.000',
      descripcion:
        'Planes sencillos para salir sin gastar mucho: parques, miradores, ferias y más.',
      presupuesto: 30000,
    },
    {
      etiqueta: '$$ Medio',
      clase: 'badge-naranja',
      titulo: '$30.000 a $80.000',
      descripcion: 'Cine, comida con amigos, talleres y salidas con un toque especial.',
      presupuesto: 80000,
    },
    {
      etiqueta: '$$$ Especial',
      clase: '',
      titulo: 'Más de $80.000',
      descripcion: 'Experiencias para celebrar: cenas, escapadas y eventos que se disfrutan.',
      presupuesto: 150000,
    },
  ];

  /** Ideas de planes populares. */
  populares: PlanModel[] = [
    {
      titulo: 'Picnic en el parque',
      detalle: 'Tarde · Aire libre',
      precio: '$25.000',
      tono: 'tono-1',
    },
    {
      titulo: 'Noche de cine y canchita',
      detalle: 'Noche · Con amigos o en pareja',
      precio: '$45.000',
      tono: 'tono-2',
    },
    {
      titulo: 'Ruta de café y brunch',
      detalle: 'Mañana · Gastronómico',
      precio: '$60.000',
      tono: 'tono-3',
    },
  ];

  /** Pasos de la sección "Cómo funciona". */
  pasos: PasoModel[] = [
    {
      numero: 1,
      titulo: 'Elige tu presupuesto',
      texto: 'Dinos cuánto quieres gastar por persona.',
    },
    {
      numero: 2,
      titulo: 'Descubre planes',
      texto: 'Te mostramos opciones según precio, distancia y tus gustos.',
    },
    {
      numero: 3,
      titulo: 'Arma tu salida',
      texto: 'Invita a tus amigos y llega con la ruta lista.',
    },
  ];
}
