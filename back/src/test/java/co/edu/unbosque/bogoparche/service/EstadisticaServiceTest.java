package co.edu.unbosque.bogoparche.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Pruebas de las series del módulo de estadísticas con repositorios simulados.
 */
@ExtendWith(MockitoExtension.class)
class EstadisticaServiceTest {
	@Mock
	private UserRepository userRepo;
	@Mock
	private CategoriaRepository categoriaRepo;
	@Mock
	private LugarRepository lugarRepo;
	@Mock
	private InvitacionRepository invitacionRepo;
	@InjectMocks
	private EstadisticaService estadisticaService;

	/**
	 * Los meses quedan como "aaaa-mm" en orden, las categorías vacías aparecen con 0 y todos los
	 * estados de invitación aparecen aunque no tengan invitaciones.
	 */
	@Test
	void armaLasSeriesDeLasGraficas() {
		when(userRepo.findAll()).thenReturn(List.of(usuario(2026, 11), usuario(2026, 3), usuario(2026, 11)));
		when(categoriaRepo.findAllByOrderByNombreAsc()).thenReturn(List.of(categoria(1L, "bailar"), categoria(2L, "comer")));
		when(lugarRepo.countByCategoriaId(1L)).thenReturn(0L);
		when(lugarRepo.countByCategoriaId(2L)).thenReturn(4L);
		when(invitacionRepo.countByEstado(EstadoInvitacion.ACEPTADA)).thenReturn(3L);

		EstadisticasDTO e = estadisticaService.calcular();
		assertEquals(List.of(new ConteoDTO("2026-03", 1), new ConteoDTO("2026-11", 2)), e.getUsuariosPorMes());
		assertEquals(List.of(new ConteoDTO("bailar", 0), new ConteoDTO("comer", 4)), e.getLugaresPorCategoria());
		assertEquals(List.of(new ConteoDTO("PENDIENTE", 0), new ConteoDTO("ACEPTADA", 3), new ConteoDTO("RECHAZADA", 0),
				new ConteoDTO("EXPIRADA", 0)), e.getInvitacionesPorEstado());
	}

	/**
	 * Crea un usuario registrado en el mes indicado.
	 *
	 * @param anio año
	 * @param mes  mes
	 * @return usuario
	 */
	private User usuario(int anio, int mes) {
		User u = new User();
		u.setFechaCreacion(LocalDateTime.of(anio, mes, 10, 8, 0));
		return u;
	}

	/**
	 * Crea una categoría.
	 *
	 * @param id     identificador
	 * @param nombre nombre
	 * @return categoría
	 */
	private Categoria categoria(Long id, String nombre) {
		Categoria c = new Categoria();
		c.setId(id);
		c.setNombre(nombre);
		return c;
	}
}
