import { Component } from '@angular/core';

interface Plan {
  titulo: string;
  detalle: string;
  precio: string;
  tono: 'tono-1' | 'tono-2' | 'tono-3';
}

interface Rango {
  etiqueta: string;
  clase: string;
  titulo: string;
  descripcion: string;
  planes: number;
}

interface Paso {
  numero: number;
  titulo: string;
  texto: string;
}

@Component({
  selector: 'app-iniciocomponent',
  standalone: false,
  templateUrl: './iniciocomponent.html',
  styleUrl: './iniciocomponent.css',
})
export class Iniciocomponent {
  destacado: Plan = {
    titulo: 'Picnic en el parque',
    detalle: 'Por persona · Tarde · Aire libre',
    precio: '$25.000',
    tono: 'tono-1',
  };

  rangos: Rango[] = [
    {
      etiqueta: '$ Económico',
      clase: 'badge-verde',
      titulo: 'Hasta $30.000',
      descripcion: 'Planes sencillos para salir sin gastar mucho: parques, miradores, ferias y más.',
      planes: 24,
    },
    {
      etiqueta: '$$ Medio',
      clase: 'badge-naranja',
      titulo: '$30.000 a $80.000',
      descripcion: 'Cine, comida con amigos, talleres y salidas con un toque especial.',
      planes: 18,
    },
    {
      etiqueta: '$$$ Especial',
      clase: '',
      titulo: 'Más de $80.000',
      descripcion: 'Experiencias para celebrar: cenas, escapadas y eventos que se disfrutan.',
      planes: 11,
    },
  ];

  populares: Plan[] = [
    { titulo: 'Picnic en el parque', detalle: 'Tarde · Aire libre', precio: '$25.000', tono: 'tono-1' },
    { titulo: 'Noche de cine y canchita', detalle: 'Noche · Con amigos o en pareja', precio: '$45.000', tono: 'tono-2' },
    { titulo: 'Ruta de café y brunch', detalle: 'Mañana · Gastronómico', precio: '$60.000', tono: 'tono-3' },
  ];

  pasos: Paso[] = [
    { numero: 1, titulo: 'Elige tu presupuesto', texto: 'Dinos cuánto quieres gastar por persona.' },
    { numero: 2, titulo: 'Descubre planes', texto: 'Te mostramos opciones según precio, distancia y tus gustos.' },
    { numero: 3, titulo: 'Arma tu salida', texto: 'Invita a tus amigos y llega con la ruta lista.' },
  ];
}
