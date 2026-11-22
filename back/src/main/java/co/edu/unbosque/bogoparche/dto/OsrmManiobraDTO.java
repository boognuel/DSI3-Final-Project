package co.edu.unbosque.bogoparche.dto;

import lombok.Data;

/**
 * Maniobra con la que empieza un paso de OSRM, por ejemplo girar a la izquierda.
 */
@Data
public class OsrmManiobraDTO {
	private String type;
	private String modifier;
}
