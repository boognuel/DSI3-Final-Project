package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.InvitacionDTO;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.EstadoSalida;
import co.edu.unbosque.model.Invitacion;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.Salida;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.InvitacionRepository;
import co.edu.unbosque.repository.SalidaRepository;
import co.edu.unbosque.repository.UserRepository;

@Service
public class InvitacionService {
	@Autowired
	private InvitacionRepository invitacionRepo;
	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private CorreoService correoService;
	@Autowired
	private AuditoriaService auditoriaService;

	@Value("${app.invitacion.dias-validez}")
	private long diasValidez;

	// 0 = ok, 1 = salida no existe, 2 = no es el organizador, 3 = salida cancelada o realizada,
	// 4 = ya invitado, 5 = falló el envío del correo, 6 = se invitó a sí mismo
	public int invitar(InvitacionDTO data, User organizador) {
		Optional<Salida> opt = salidaRepo.findById(data.getSalidaId());
		if (opt.isEmpty()) {
			return 1;
		}
		Salida salida = opt.get();
		if (!salida.getOrganizador().getId().equals(organizador.getId())) {
			return 2;
		}
		if (salida.getEstado() == EstadoSalida.CANCELADA || salida.getEstado() == EstadoSalida.REALIZADA) {
			return 3;
		}
		String email = data.getEmailInvitado().trim().toLowerCase();
		if (email.equalsIgnoreCase(organizador.getEmail())) {
			return 6;
		}
		LocalDateTime ahora = LocalDateTime.now();
		for (Invitacion i : invitacionRepo.findBySalidaIdAndEmailInvitadoIgnoreCase(salida.getId(), email)) {
			if (i.getEstado() == EstadoInvitacion.ACEPTADA) {
				return 4;
			}
			if (i.getEstado() == EstadoInvitacion.PENDIENTE) {
				if (i.getFechaExpiracion().isAfter(ahora)) {
					return 4;
				}
				i.setEstado(EstadoInvitacion.EXPIRADA);
				invitacionRepo.save(i);
			}
		}
		Invitacion inv = new Invitacion();
		inv.setSalida(salida);
		inv.setEmailInvitado(email);
		inv.setUsuarioInvitado(userRepo.findByEmail(email).orElse(null));
		inv.setToken(UUID.randomUUID().toString());
		inv.setEstado(EstadoInvitacion.PENDIENTE);
		inv.setFechaEnvio(ahora);
		inv.setFechaExpiracion(ahora.plusDays(diasValidez));
		invitacionRepo.save(inv);
		if (!correoService.enviarInvitacion(inv)) {
			invitacionRepo.delete(inv);
			auditoriaService.registrar(organizador, AccionAuditoria.ENVIAR_INVITACION, "Salida", salida.getId(),
					"Falló el envío del correo a " + email, ResultadoAuditoria.FALLO);
			return 5;
		}
		auditoriaService.registrar(organizador, AccionAuditoria.ENVIAR_INVITACION, "Invitacion", inv.getId(),
				"Invitado: " + email, ResultadoAuditoria.EXITO);
		return 0;
	}

	// Lo llama el invitado desde el enlace del correo (el token es la prueba de que tiene acceso al correo).
	// 0 = ok, 1 = token inválido, 2 = ya respondida o vencida, 3 = acaba de vencer
	public int responder(String token, boolean aceptar) {
		Optional<Invitacion> opt = invitacionRepo.findByToken(token);
		if (opt.isEmpty()) {
			return 1;
		}
		Invitacion inv = opt.get();
		if (inv.getEstado() != EstadoInvitacion.PENDIENTE) {
			return 2;
		}
		LocalDateTime ahora = LocalDateTime.now();
		if (inv.getFechaExpiracion().isBefore(ahora)) {
			inv.setEstado(EstadoInvitacion.EXPIRADA);
			invitacionRepo.save(inv);
			return 3;
		}
		inv.setEstado(aceptar ? EstadoInvitacion.ACEPTADA : EstadoInvitacion.RECHAZADA);
		inv.setFechaRespuesta(ahora);
		invitacionRepo.save(inv);
		auditoriaService.registrarSinSesion(inv.getEmailInvitado(),
				aceptar ? AccionAuditoria.ACEPTAR_INVITACION : AccionAuditoria.RECHAZAR_INVITACION,
				"Invitación " + inv.getId() + " de la salida " + inv.getSalida().getId(), ResultadoAuditoria.EXITO);
		return 0;
	}

	public List<InvitacionDTO> getBySalida(Long salidaId) {
		List<InvitacionDTO> lista = new ArrayList<>();
		invitacionRepo.findBySalidaId(salidaId).forEach(i -> lista.add(toDTO(i)));
		return lista;
	}

	public List<InvitacionDTO> getMisInvitaciones(User u) {
		List<InvitacionDTO> lista = new ArrayList<>();
		invitacionRepo.findByEmailInvitadoIgnoreCase(u.getEmail()).forEach(i -> lista.add(toDTO(i)));
		return lista;
	}

	private InvitacionDTO toDTO(Invitacion i) {
		InvitacionDTO d = new InvitacionDTO();
		d.setId(i.getId());
		d.setSalidaId(i.getSalida().getId());
		d.setSalidaTitulo(i.getSalida().getTitulo());
		d.setEmailInvitado(i.getEmailInvitado());
		d.setEstado(i.getEstado());
		d.setFechaEnvio(i.getFechaEnvio());
		d.setFechaExpiracion(i.getFechaExpiracion());
		d.setFechaRespuesta(i.getFechaRespuesta());
		return d;
	}
}
