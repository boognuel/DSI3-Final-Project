package co.edu.unbosque.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.exception.RecursoExpiradoException;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.Invitacion;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.Salida;
import co.edu.unbosque.repository.InvitacionRepository;
import co.edu.unbosque.repository.SalidaRepository;
import co.edu.unbosque.repository.UserRepository;

/**
 * Pruebas de la respuesta a las invitaciones con repositorios y correo simulados.
 */
@ExtendWith(MockitoExtension.class)
class InvitacionServiceTest {
	@Mock
	private InvitacionRepository invitacionRepo;
	@Mock
	private SalidaRepository salidaRepo;
	@Mock
	private UserRepository userRepo;
	@Mock
	private CorreoService correoService;
	@Mock
	private AuditoriaService auditoriaService;
	@InjectMocks
	private InvitacionService invitacionService;

	private Invitacion invitacion;

	/**
	 * Prepara una invitación pendiente que vence mañana.
	 */
	@BeforeEach
	void setUp() {
		Salida s = new Salida();
		s.setId(4L);
		invitacion = new Invitacion();
		invitacion.setId(9L);
		invitacion.setSalida(s);
		invitacion.setEmailInvitado("luis@example.com");
		invitacion.setToken("tok");
		invitacion.setFechaExpiracion(LocalDateTime.now().plusDays(1));
	}

	/**
	 * Al aceptar, la invitación queda aceptada y se avisa al organizador.
	 */
	@Test
	void aceptarAvisaAlOrganizador() {
		when(invitacionRepo.findByToken("tok")).thenReturn(Optional.of(invitacion));
		when(correoService.enviarRespuestaInvitacion(invitacion)).thenReturn(true);
		invitacionService.responder("tok", true);
		assertEquals(EstadoInvitacion.ACEPTADA, invitacion.getEstado());
		verify(correoService).enviarRespuestaInvitacion(invitacion);
		verify(auditoriaService, never()).registrarSinSesion(anyString(), any(), anyString(),
				eq(ResultadoAuditoria.FALLO));
	}

	/**
	 * Si el aviso falla, el rechazo igual queda guardado y la falla queda en la auditoría.
	 */
	@Test
	void rechazoSeGuardaAunqueFalleElAviso() {
		when(invitacionRepo.findByToken("tok")).thenReturn(Optional.of(invitacion));
		when(correoService.enviarRespuestaInvitacion(invitacion)).thenReturn(false);
		invitacionService.responder("tok", false);
		assertEquals(EstadoInvitacion.RECHAZADA, invitacion.getEstado());
		verify(invitacionRepo).save(invitacion);
		verify(auditoriaService).registrarSinSesion(eq("luis@example.com"), eq(AccionAuditoria.RECHAZAR_INVITACION),
				anyString(), eq(ResultadoAuditoria.FALLO));
	}

	/**
	 * Una invitación vencida no se responde ni se avisa al organizador.
	 */
	@Test
	void invitacionVencidaNoAvisa() {
		invitacion.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
		when(invitacionRepo.findByToken("tok")).thenReturn(Optional.of(invitacion));
		assertThrows(RecursoExpiradoException.class, () -> invitacionService.responder("tok", true));
		assertEquals(EstadoInvitacion.EXPIRADA, invitacion.getEstado());
		verify(correoService, never()).enviarRespuestaInvitacion(any());
	}
}
