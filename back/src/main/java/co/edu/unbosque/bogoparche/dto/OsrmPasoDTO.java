package co.edu.unbosque.bogoparche.dto;

import lombok.Data;

/**
 * Paso de un tramo de OSRM: la vía, su distancia en metros y la maniobra.
 */
@Data
public class OsrmPasoDTO {
	private String name;
	private double distance;
	private OsrmManiobraDTO maneuver;
}
