package co.edu.unboque.dto;

import java.math.BigDecimal;
import lombok.Data;

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
