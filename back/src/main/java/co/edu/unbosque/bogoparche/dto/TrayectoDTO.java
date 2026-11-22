package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Camino que calcula OSRM: distancia, duración, la línea en GeoJSON para dibujarla en el mapa y
 * los pasos del recorrido, por ejemplo "turn left Carrera 15 (1200 m)".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrayectoDTO {
	private int distanciaMetros;
	private int duracionSegundos;
	private String geometriaGeoJson;
	private List<String> pasos;
}
