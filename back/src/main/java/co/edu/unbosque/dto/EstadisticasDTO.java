package co.edu.unbosque.dto;

import java.util.List;

/**
 * Datos para las gráficas del módulo de estadísticas del administrador. Cada serie es una lista de
 * pares etiqueta-total, lista para pasarla a una gráfica.
 *
 * @param usuariosPorMes        usuarios registrados por mes, con etiquetas "aaaa-mm"
 * @param lugaresPorCategoria   lugares del catálogo por categoría, incluidas las vacías
 * @param invitacionesPorEstado invitaciones por estado: pendiente, aceptada, rechazada y expirada
 */
public record EstadisticasDTO(List<Conteo> usuariosPorMes, List<Conteo> lugaresPorCategoria,
		List<Conteo> invitacionesPorEstado) {

	/**
	 * Un punto de una serie.
	 *
	 * @param etiqueta nombre del punto, por ejemplo "2026-11" o "comer"
	 * @param total    cantidad
	 */
	public record Conteo(String etiqueta, long total) {
	}
}
