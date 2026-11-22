package co.edu.unbosque.bogoparche.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un punto de una serie de las gráficas: una etiqueta, por ejemplo "2026-11" o "comer", y su total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConteoDTO {
	private String etiqueta;
	private long total;
}
