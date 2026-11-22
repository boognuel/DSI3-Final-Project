package co.edu.unbosque.bogoparche.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.ConteoDTO;
import co.edu.unbosque.bogoparche.dto.EstadisticasDTO;
import co.edu.unbosque.bogoparche.model.Categoria;
import co.edu.unbosque.bogoparche.model.EstadoInvitacion;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.CategoriaRepository;
import co.edu.unbosque.bogoparche.repository.InvitacionRepository;
import co.edu.unbosque.bogoparche.repository.LugarRepository;
import co.edu.unbosque.bogoparche.repository.UserRepository;

/**
 * Calcula los datos del módulo de estadísticas del administrador.
 */
@Service
public class EstadisticaService {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private CategoriaRepository categoriaRepo;
	@Autowired
	private LugarRepository lugarRepo;
	@Autowired
	private InvitacionRepository invitacionRepo;

	/**
	 * Calcula las series de las gráficas. Todas las categorías y todos los estados aparecen, aunque
	 * tengan cero, para que las gráficas no cambien de forma.
	 *
	 * @return usuarios por mes, lugares por categoría e invitaciones por estado
	 */
	public EstadisticasDTO calcular() {
		Map<String, Long> porMes = new TreeMap<>();
		for (User u : userRepo.findAll()) {
			if (u.getFechaCreacion() != null) {
				String mes = u.getFechaCreacion().toString().substring(0, 7);
				porMes.put(mes, porMes.getOrDefault(mes, 0L) + 1);
			}
		}
		List<ConteoDTO> usuarios = new ArrayList<>();
		for (Map.Entry<String, Long> e : porMes.entrySet()) {
			usuarios.add(new ConteoDTO(e.getKey(), e.getValue()));
		}

		List<ConteoDTO> lugares = new ArrayList<>();
		for (Categoria c : categoriaRepo.findAllByOrderByNombreAsc()) {
			lugares.add(new ConteoDTO(c.getNombre(), lugarRepo.countByCategoriaId(c.getId())));
		}

		List<ConteoDTO> invitaciones = new ArrayList<>();
		for (EstadoInvitacion e : EstadoInvitacion.values()) {
			invitaciones.add(new ConteoDTO(e.name(), invitacionRepo.countByEstado(e)));
		}
		return new EstadisticasDTO(usuarios, lugares, invitaciones);
	}
}
