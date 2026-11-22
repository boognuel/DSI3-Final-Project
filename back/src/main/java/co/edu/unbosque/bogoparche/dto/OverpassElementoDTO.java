package co.edu.unbosque.bogoparche.dto;

import java.util.Map;
import lombok.Data;

/**
 * Elemento de OpenStreetMap que devuelve Overpass: un punto (node), una vía (way) o una relación,
 * con sus etiquetas, por ejemplo name, amenity o addr:street.
 */
@Data
public class OverpassElementoDTO {
	private String type;
	private Long id;
	private Double lat;
	private Double lon;
	private OverpassCentroDTO center;
	private Map<String, String> tags;
}
