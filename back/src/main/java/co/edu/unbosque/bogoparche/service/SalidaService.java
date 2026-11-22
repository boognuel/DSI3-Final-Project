package co.edu.unbosque.bogoparche.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.SalidaDTO;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.RecursoNoEncontradoException;
import co.edu.unbosque.bogoparche.model.EstadoInvitacion;
import co.edu.unbosque.bogoparche.model.Invitacion;
import co.edu.unbosque.bogoparche.model.EstadoSalida;
import co.edu.unbosque.bogoparche.model.Salida;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.InvitacionRepository;
import co.edu.unbosque.bogoparche.repository.SalidaRepository;
import co.edu.unbosque.bogoparche.repository.UserRepository;

/**
 * Gestión de las salidas que organizan los usuarios.
 */
@Service
public class SalidaService implements CRUDOperation<SalidaDTO> {
	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private InvitacionRepository invitacionRepo;

	/**
	 * Cuenta las salidas.
	 *
	 * @return número de salidas
	 */
	@Override
	public long count() {
		return salidaRepo.count();
	}

	/**
	 * Indica si existe una salida.
	 *
	 * @param id identificador de la salida
	 * @return {@code true} si existe
	 */
	@Override
	public boolean exist(Long id) {
		return salidaRepo.existsById(id);
	}

	/**
	 * Crea una salida en estado {@link EstadoSalida#PLANEANDO} y deja su identificador en el DTO.
	 *
	 * @param data datos de la salida, con el identificador del organizador
	 * @throws RecursoNoEncontradoException si el organizador no existe
	 * @throws DatosInvalidosException      si el presupuesto es nulo o negativo
	 */
	@Override
	public void create(SalidaDTO data) {
		User org = userRepo.findById(data.getOrganizadorId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Organizador no encontrado"));
		validarPresupuesto(data);
		Salida s = new Salida();
		s.setOrganizador(org);
		copiar(data, s);
		s.setEstado(EstadoSalida.PLANEANDO);
		salidaRepo.save(s);
		data.setId(s.getId());
	}

	/**
	 * Lista todas las salidas.
	 *
	 * @return salidas
	 */
	@Override
	public List<SalidaDTO> getAll() {
		List<SalidaDTO> lista = new ArrayList<>();
		salidaRepo.findAll().forEach(s -> lista.add(toDTO(s)));
		return lista;
	}

	/**
	 * Elimina una salida.
	 *
	 * @param id identificador de la salida
	 * @throws RecursoNoEncontradoException si la salida no existe
	 */
	@Override
	public void deleteById(Long id) {
		salidaRepo.delete(buscar(id));
	}

	/**
	 * Actualiza los datos de una salida y, si se envía, su estado.
	 *
	 * @param id      identificador de la salida
	 * @param newData datos nuevos
	 * @throws RecursoNoEncontradoException si la salida no existe
	 * @throws DatosInvalidosException      si el presupuesto es nulo o negativo
	 */
	@Override
	public void updateById(Long id, SalidaDTO newData) {
		Salida s = buscar(id);
		validarPresupuesto(newData);
		copiar(newData, s);
		if (newData.getEstado() != null) {
			s.setEstado(newData.getEstado());
		}
		salidaRepo.save(s);
	}

	/**
	 * Obtiene una salida por su identificador.
	 *
	 * @param id identificador de la salida
	 * @return datos de la salida
	 * @throws RecursoNoEncontradoException si la salida no existe
	 */
	public SalidaDTO getById(Long id) {
		return toDTO(buscar(id));
	}

	/**
	 * Busca la entidad de una salida.
	 *
	 * @param id identificador de la salida
	 * @return la salida
	 * @throws RecursoNoEncontradoException si no existe
	 */
	private Salida buscar(Long id) {
		return salidaRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Salida no encontrada"));
	}

	/**
	 * Valida que el presupuesto exista y no sea negativo.
	 *
	 * @param d datos de la salida
	 * @throws DatosInvalidosException si el presupuesto no es válido
	 */
	private void validarPresupuesto(SalidaDTO d) {
		if (d.getPresupuesto() == null || d.getPresupuesto().signum() < 0) {
			throw new DatosInvalidosException("Presupuesto no válido");
		}
	}

	/**
	 * Lista las salidas que organiza el usuario y aquellas en las que aceptó una invitación.
	 *
	 * @param u usuario autenticado
	 * @return salidas del usuario, sin repetir
	 */
	public List<SalidaDTO> getMisSalidas(User u) {
		Map<Long, Salida> todas = new LinkedHashMap<>();
		salidaRepo.findByOrganizadorId(u.getId()).forEach(s -> todas.put(s.getId(), s));
		for (Invitacion i : invitacionRepo.findByEmailInvitadoIgnoreCase(u.getEmail())) {
			if (i.getEstado() == EstadoInvitacion.ACEPTADA) {
				todas.putIfAbsent(i.getSalida().getId(), i.getSalida());
			}
		}
		List<SalidaDTO> lista = new ArrayList<>();
		todas.values().forEach(s -> lista.add(toDTO(s)));
		return lista;
	}

	/**
	 * Indica si el usuario organiza la salida.
	 *
	 * @param salidaId identificador de la salida
	 * @param userId   identificador del usuario
	 * @return {@code true} si es el organizador
	 */
	public boolean esOrganizador(Long salidaId, Long userId) {
		return salidaRepo.findById(salidaId).map(s -> s.getOrganizador().getId().equals(userId)).orElse(false);
	}

	/**
	 * Indica si el usuario organiza la salida o aceptó una invitación a ella.
	 *
	 * @param salidaId identificador de la salida
	 * @param u        usuario autenticado
	 * @return {@code true} si participa
	 */
	public boolean esParticipante(Long salidaId, User u) {
		return esOrganizador(salidaId, u.getId()) || invitacionRepo
				.existsBySalidaIdAndEmailInvitadoIgnoreCaseAndEstado(salidaId, u.getEmail(), EstadoInvitacion.ACEPTADA);
	}

	/**
	 * Copia los datos editables del DTO a la entidad.
	 *
	 * @param d datos recibidos
	 * @param s salida que se modifica
	 */
	private void copiar(SalidaDTO d, Salida s) {
		s.setTitulo(d.getTitulo());
		s.setDescripcion(d.getDescripcion());
		s.setPresupuesto(d.getPresupuesto());
		s.setZonaNombre(d.getZonaNombre());
		s.setLatitudCentro(d.getLatitudCentro());
		s.setLongitudCentro(d.getLongitudCentro());
		s.setRadioMetros(d.getRadioMetros());
		s.setFechaHora(d.getFechaHora());
	}

	/**
	 * Convierte la entidad en DTO, con el nombre del organizador.
	 *
	 * @param s salida
	 * @return datos de la salida
	 */
	private SalidaDTO toDTO(Salida s) {
		SalidaDTO d = new SalidaDTO();
		d.setId(s.getId());
		d.setTitulo(s.getTitulo());
		d.setDescripcion(s.getDescripcion());
		d.setOrganizadorId(s.getOrganizador().getId());
		d.setOrganizadorNombre(s.getOrganizador().getUsername());
		d.setPresupuesto(s.getPresupuesto());
		d.setZonaNombre(s.getZonaNombre());
		d.setLatitudCentro(s.getLatitudCentro());
		d.setLongitudCentro(s.getLongitudCentro());
		d.setRadioMetros(s.getRadioMetros());
		d.setFechaHora(s.getFechaHora());
		d.setEstado(s.getEstado());
		return d;
	}
}
