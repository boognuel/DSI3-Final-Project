import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import * as L from 'leaflet';
import { MarcadorModel } from '../../models/marcador.model';

/**
 * Mapa de Leaflet con la ubicación del usuario, los lugares y, si se indica, una ruta.
 */
@Component({
  selector: 'app-mapacomponent',
  standalone: false,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './mapacomponent.css',
  templateUrl: './mapacomponent.html',
})
export class Mapacomponent implements AfterViewInit, OnChanges, OnDestroy {
  /** Latitud del usuario. */
  @Input() latitud: number | null = null;
  /** Longitud del usuario. */
  @Input() longitud: number | null = null;
  /** Marcadores del mapa. */
  @Input() marcadores: MarcadorModel[] = [];
  /** Puntos de la ruta como pares latitud, longitud. */
  @Input() ruta: number[][] = [];
  /** Alto del mapa en CSS. */
  @Input() alto: string = '420px';
  /** Elemento que contiene el mapa. */
  @ViewChild('contenedor') contenedor!: ElementRef<HTMLDivElement>;

  /** Mapa de Leaflet. */
  private mapa: L.Map | null = null;
  /** Capa donde se dibujan los marcadores y la ruta. */
  private capa: L.LayerGroup | null = null;

  /**
   * Crea el mapa cuando el contenedor ya existe.
   */
  ngAfterViewInit(): void {
    this.mapa = L.map(this.contenedor.nativeElement, { zoomControl: true }).setView(
      [4.705, -74.045],
      13,
    );
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; colaboradores de OpenStreetMap',
    }).addTo(this.mapa);
    this.capa = L.layerGroup().addTo(this.mapa);
    this.dibujar();
  }

  /**
   * Vuelve a dibujar cuando cambian las entradas.
   */
  ngOnChanges(): void {
    this.dibujar();
  }

  /**
   * Libera los recursos al salir de la página.
   */
  ngOnDestroy(): void {
    if (this.mapa !== null) {
      this.mapa.remove();
      this.mapa = null;
    }
  }

  /**
   * Dibuja la ubicación, los marcadores y la ruta.
   */
  private dibujar(): void {
    if (this.mapa === null || this.capa === null) {
      return;
    }
    this.capa.clearLayers();
    const puntos: L.LatLng[] = [];
    if (typeof this.latitud === 'number' && typeof this.longitud === 'number') {
      const yo: L.LatLng = L.latLng(this.latitud, this.longitud);
      L.circleMarker(yo, {
        radius: 9,
        color: '#140336',
        weight: 3,
        fillColor: '#feca03',
        fillOpacity: 1,
      })
        .bindTooltip('Estás aquí')
        .addTo(this.capa);
      puntos.push(yo);
    }
    for (const m of this.marcadores) {
      if (typeof m.latitud !== 'number' || typeof m.longitud !== 'number') {
        continue;
      }
      const punto: L.LatLng = L.latLng(m.latitud, m.longitud);
      L.circleMarker(punto, {
        radius: m.destacado ? 10 : 7,
        color: '#140336',
        weight: 2,
        fillColor: m.destacado ? '#e85d8a' : '#f28b4b',
        fillOpacity: 0.95,
      })
        .bindTooltip(m.titulo)
        .addTo(this.capa);
      puntos.push(punto);
    }
    if (this.ruta.length > 1) {
      const linea: L.LatLng[] = [];
      for (const par of this.ruta) {
        if (par.length === 2 && typeof par[0] === 'number' && typeof par[1] === 'number') {
          linea.push(L.latLng(par[0], par[1]));
        }
      }
      L.polyline(linea, { color: '#140336', weight: 5, opacity: 0.85 }).addTo(this.capa);
      for (const p of linea) {
        puntos.push(p);
      }
    }
    if (puntos.length === 1) {
      this.mapa.setView(puntos[0], 15);
    } else if (puntos.length > 1) {
      this.mapa.fitBounds(L.latLngBounds(puntos), { padding: [30, 30] });
    }
  }
}
