package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.InvitacionDTO;
import co.edu.unbosque.exception.AccesoDenegadoException;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.exception.RecursoExpiradoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.exception.ServicioExternoException;
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

/**
 * Envío de invitaciones a las salidas y registro de las respuestas de los invitados.
 */
@Service
public class InvitacionService {
	private static final String INVITACION_VIGENTE = "Esa persona ya tiene una invitación vigente";

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

	/**
	 * Crea una invitación a la salida y la envía por correo.
	 *
	 * @param data        salida y correo del invitado
	 * @param organizador usuario que invita
	 * @throws RecursoNoEncontradoException si la salida no existe
	 * @throws AccesoDenegadoException      si quien invita no organiza la salida
	 * @throws DatosInvalidosException      si la salida está cancelada o realizada, o el
	 *                                      organizador se invita a sí mismo
	 * @throws ConflictoException           si el invitado ya aceptó o tiene una invitación vigente
	 * @throws ServicioExternoException     si no se pudo enviar el correo
	 */
	public void invitar(InvitacionDTO data, User organizador) {
		Salida salida = salidaRepo.findById(data.getSalidaId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Salida no encontrada"));
		if (!salida.getOrganizador().getId().equals(organizador.getId())) {
			throw new AccesoDenegadoException("Solo el organizador puede invitar");
		}
		if (salida.getEstado() == EstadoSalida.CANCELADA || salida.getEstado() == EstadoSalida.REALIZADA) {
			throw new DatosInvalidosException("La salida está cancelada o ya se realizó");
		}
		String email = data.getEmailInvitado().trim().toLowerCase();
		if (email.equalsIgnoreCase(organizador.getEmail())) {
			throw new DatosInvalidosException("No puedes invitarte a ti mismo");
		}
		LocalDateTime ahora = LocalDateTime.now();
		for (Invitacion i : invitacionRepo.findBySalidaIdAndEmailInvitadoIgnoreCase(salida.getId(), email)) {
			if (i.getEstado() == EstadoInvitacion.ACEPTADA) {
				throw new ConflictoException(INVITACION_VIGENTE);
			}
			if (i.getEstado() == EstadoInvitacion.PENDIENTE) {
				if (i.getFechaExpiracion().isAfter(ahora)) {
					throw new ConflictoException(INVITACION_VIGENTE);
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
			throw new ServicioExternoException("No se pudo enviar el correo");
		}
		auditoriaService.registrar(organizador, AccionAuditoria.ENVIAR_INVITACION, "Invitacion", inv.getId(),
				"Invitado: " + email, ResultadoAuditoria.EXITO);
	}

	/**
	 * Registra la respuesta del invitado. Lo llama el invitado desde el enlace del correo; el token
	 * es la prueba de que tiene acceso a ese correo.
	 *
	 * @param token   token de la invitación
	 * @param aceptar {@code true} para aceptar, {@code false} para rechazar
	 * @throws RecursoNoEncontradoException si el token no corresponde a ninguna invitación
	 * @throws ConflictoException           si la invitación ya fue respondida o había vencido
	 * @throws RecursoExpiradoException     si la invitación venció; queda marcada como expirada
	 */
	public void responder(String token, boolean aceptar) {
		Invitacion inv = invitacionRepo.findByToken(token)
				.orElseThrow(() -> new RecursoNoEncontradoException("Invitación no encontrada"));
		if (inv.getEstado() != EstadoInvitacion.PENDIENTE) {
			throw new ConflictoException("Esta invitación ya fue respondida");
		}
		LocalDateTime ahora = LocalDateTime.now();
		if (inv.getFechaExpiracion().isBefore(ahora)) {
			inv.setEstado(EstadoInvitacion.EXPIRADA);
			invitacionRepo.save(inv);
			throw new RecursoExpiradoException("La invitación venció");
		}
		inv.setEstado(aceptar ? EstadoInvitacion.ACEPTADA : EstadoInvitacion.RECHAZADA);
		inv.setFechaRespuesta(ahora);
		invitacionRepo.save(inv);
		auditoriaService.registrarSinSesion(inv.getEmailInvitado(),
				aceptar ? AccionAuditoria.ACEPTAR_INVITACION : AccionAuditoria.RECHAZAR_INVITACION,
				"Invitación " + inv.getId() + " de la salida " + inv.getSalida().getId(), ResultadoAuditoria.EXITO);
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
