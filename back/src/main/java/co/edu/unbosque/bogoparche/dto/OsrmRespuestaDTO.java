package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.Data;

/**
 * Respuesta de OSRM. El código es "Ok" si encontró ruta y "NoRoute" si no hay camino.
 */
@Data
public class OsrmRespuestaDTO {
	private String code;
	private List<OsrmRutaDTO> routes;
}
