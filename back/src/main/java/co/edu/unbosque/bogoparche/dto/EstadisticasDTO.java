package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos para las gráficas del módulo de estadísticas del administrador: usuarios registrados por
 * mes, lugares por categoría e invitaciones por estado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
	private List<ConteoDTO> usuariosPorMes;
	private List<ConteoDTO> lugaresPorCategoria;
	private List<ConteoDTO> invitacionesPorEstado;
}
