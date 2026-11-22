package co.edu.unbosque.bogoparche.dto;

import lombok.Data;

/**
 * Centro de un edificio o un área de OpenStreetMap, que Overpass entrega en lugar de un punto.
 */
@Data
public class OverpassCentroDTO {
	private Double lat;
	private Double lon;
}
