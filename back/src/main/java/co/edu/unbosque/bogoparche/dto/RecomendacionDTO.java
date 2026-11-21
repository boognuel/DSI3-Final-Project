package co.edu.unbosque.bogoparche.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Planes que se pueden hacer con un presupuesto: las zonas a las que alcanza y los lugares, con sus
 * coordenadas para mostrarlos en el mapa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionDTO {
	private BigDecimal presupuesto;
	private List<ZonaDTO> zonas;
	private List<LugarDTO> lugares;
}
