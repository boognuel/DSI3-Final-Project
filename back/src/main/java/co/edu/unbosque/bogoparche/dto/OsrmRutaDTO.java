package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import com.google.gson.JsonObject;
import lombok.Data;

/**
 * Ruta de OSRM: distancia en metros, duración en segundos, línea en GeoJSON y tramos.
 */
@Data
public class OsrmRutaDTO {
	private double distance;
	private double duration;
	private JsonObject geometry;
	private List<OsrmTramoDTO> legs;
}
