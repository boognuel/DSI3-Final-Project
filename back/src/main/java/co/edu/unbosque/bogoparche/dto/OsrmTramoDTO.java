package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.Data;

/**
 * Tramo de una ruta de OSRM con sus pasos.
 */
@Data
public class OsrmTramoDTO {
	private List<OsrmPasoDTO> steps;
}
