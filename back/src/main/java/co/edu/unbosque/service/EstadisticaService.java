package co.edu.unbosque.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.EstadisticasDTO;
import co.edu.unbosque.dto.EstadisticasDTO.Conteo;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.repository.InvitacionRepository;
import co.edu.unbosque.repository.LugarRepository;
import co.edu.unbosque.repository.UserRepository;

/**
 * Calcula los datos del módulo de estadísticas del administrador.
 */
@Service
public class EstadisticaService {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private LugarRepository lugarRepo;
	@Autowired
	private InvitacionRepository invitacionRepo;

	/**
	 * Calcula las series de las gráficas.
	 *
	 * @return usuarios por mes, lugares por categoría e invitaciones por estado
	 */
	public EstadisticasDTO calcular() {
		List<Conteo> usuarios = new ArrayList<>();
		for (Object[] f : userRepo.contarPorMes()) {
			usuarios.add(new Conteo(String.format(Locale.ROOT, "%04d-%02d", ((Number) f[0]).intValue(),
					((Number) f[1]).intValue()), ((Number) f[2]).longValue()));
		}

		List<Conteo> lugares = new ArrayList<>();
		for (Object[] f : lugarRepo.contarPorCategoria()) {
			lugares.add(new Conteo((String) f[0], ((Number) f[1]).longValue()));
		}

		// Todos los estados aparecen, aunque no haya invitaciones en alguno, para que la gráfica no cambie
		Map<EstadoInvitacion, Long> porEstado = new EnumMap<>(EstadoInvitacion.class);
		for (EstadoInvitacion e : EstadoInvitacion.values()) {
			porEstado.put(e, 0L);
		}
		for (Object[] f : invitacionRepo.contarPorEstado()) {
			if (f[0] != null) {
				porEstado.put((EstadoInvitacion) f[0], ((Number) f[1]).longValue());
			}
		}
		List<Conteo> invitaciones = porEstado.entrySet().stream()
				.map(e -> new Conteo(e.getKey().name(), e.getValue())).toList();

		return new EstadisticasDTO(usuarios, lugares, invitaciones);
	}
}
