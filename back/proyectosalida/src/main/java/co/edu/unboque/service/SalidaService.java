package co.edu.unboque.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unboque.dto.SalidaDTO;
import co.edu.unboque.model.EstadoInvitacion;
import co.edu.unboque.model.EstadoSalida;
import co.edu.unboque.model.Salida;
import co.edu.unboque.model.User;
import co.edu.unboque.repository.InvitacionRepository;
import co.edu.unboque.repository.SalidaRepository;
import co.edu.unboque.repository.UserRepository;

@Service
public class SalidaService implements CRUDOperation<SalidaDTO> {
	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private InvitacionRepository invitacionRepo;

	@Override
	public long count() {
		return salidaRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return salidaRepo.existsById(id);
	}

	// 0 = ok, 1 = organizador no existe, 2 = presupuesto no válido. Deja el id en el DTO.
	@Override
	public int create(SalidaDTO data) {
		Optional<User> org = userRepo.findById(data.getOrganizadorId());
		if (org.isEmpty()) {
			return 1;
		}
		if (data.getPresupuesto() == null || data.getPresupuesto().signum() < 0) {
			return 2;
		}
		Salida s = new Salida();
		s.setOrganizador(org.get());
		copiar(data, s);
		s.setEstado(EstadoSalida.PLANEANDO);
		salidaRepo.save(s);
		data.setId(s.getId());
		return 0;
	}

	@Override
	public List<SalidaDTO> getAll() {
		List<SalidaDTO> lista = new ArrayList<>();
		salidaRepo.findAll().forEach(s -> lista.add(toDTO(s)));
		return lista;
	}

	@Override
	public int deleteById(Long id) {
		Optional<Salida> found = salidaRepo.findById(id);
		if (found.isPresent()) {
			salidaRepo.delete(found.get());
			return 0;
		}
		return 1;
	}

	// 0 = ok, 1 = no existe, 2 = presupuesto no válido
	@Override
	public int updateById(Long id, SalidaDTO newData) {
		Optional<Salida> found = salidaRepo.findById(id);
		if (found.isEmpty()) {
			return 1;
		}
		if (newData.getPresupuesto() == null || newData.getPresupuesto().signum() < 0) {
			return 2;
		}
		Salida s = found.get();
		copiar(newData, s);
		if (newData.getEstado() != null) {
			s.setEstado(newData.getEstado());
		}
		salidaRepo.save(s);
		return 0;
	}

	public SalidaDTO getById(Long id) {
		return salidaRepo.findById(id).map(this::toDTO).orElse(null);
	}

	// Salidas que organiza el usuario + salidas donde aceptó una invitación
	public List<SalidaDTO> getMisSalidas(User u) {
		Map<Long, Salida> todas = new LinkedHashMap<>();
		salidaRepo.findByOrganizadorId(u.getId()).forEach(s -> todas.put(s.getId(), s));
		salidaRepo.findByInvitadoConEstado(u.getEmail(), EstadoInvitacion.ACEPTADA)
				.forEach(s -> todas.putIfAbsent(s.getId(), s));
		List<SalidaDTO> lista = new ArrayList<>();
		todas.values().forEach(s -> lista.add(toDTO(s)));
		return lista;
	}

	public boolean esOrganizador(Long salidaId, Long userId) {
		return salidaRepo.findById(salidaId).map(s -> s.getOrganizador().getId().equals(userId)).orElse(false);
	}

	public boolean esParticipante(Long salidaId, User u) {
		return esOrganizador(salidaId, u.getId()) || invitacionRepo
				.existsBySalidaIdAndEmailInvitadoIgnoreCaseAndEstado(salidaId, u.getEmail(), EstadoInvitacion.ACEPTADA);
	}

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
