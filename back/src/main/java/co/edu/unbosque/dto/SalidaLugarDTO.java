package co.edu.unbosque.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Lugar propuesto en una salida, con su orden, si es el destino elegido y el costo por persona.
 */
@Data
public class SalidaLugarDTO {
	private Long id;
	private Long salidaId;
	private Long lugarId;
	private String lugarNombre;
	private String categoria;
	private Double latitud;
	private Double longitud;
	private int orden;
	private boolean seleccionado;
	private BigDecimal costoEstimadoPorPersona;
}
