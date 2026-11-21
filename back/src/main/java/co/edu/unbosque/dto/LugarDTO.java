package co.edu.unbosque.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Datos de un lugar del catálogo. Al crear o editar se envían los identificadores de la categoría
 * y la zona; en las respuestas también llegan sus nombres.
 */
@Data
public class LugarDTO {
	private Long id;
	private Long osmId;
	private String osmTipo;
	private String nombre;
	private String categoria;
	private String direccion;
	private Double latitud;
	private Double longitud;
	private BigDecimal costoEstimado;
}
