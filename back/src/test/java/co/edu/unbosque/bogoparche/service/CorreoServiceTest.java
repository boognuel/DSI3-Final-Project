package co.edu.unbosque.bogoparche.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import co.edu.unbosque.bogoparche.model.EstadoInvitacion;
import co.edu.unbosque.bogoparche.model.Invitacion;
import co.edu.unbosque.bogoparche.model.Salida;
import co.edu.unbosque.bogoparche.model.User;
import jakarta.mail.internet.MimeMessage;

/**
 * Pruebas de CorreoService con un JavaMailSender simulado: no se envían correos reales.
 */
class CorreoServiceTest {
	private JavaMailSender mailSender;
	private CorreoService correoService;

	/**
	 * Crea el servicio con el servidor de correo simulado y la configuración de las propiedades.
	 */
	@BeforeEach
	void setUp() {
		mailSender = mock(JavaMailSender.class);
		when(mailSender.createMimeMessage()).thenAnswer(i -> new JavaMailSenderImpl().createMimeMessage());
		correoService = new CorreoService();
		ReflectionTestUtils.setField(correoService, "mailSender", mailSender);
		ReflectionTestUtils.setField(correoService, "remitente", "bogoparche@example.com");
		ReflectionTestUtils.setField(correoService, "frontendUrl", "http://localhost:4200");
		ReflectionTestUtils.setField(correoService, "horasVerificacion", 24L);
		ReflectionTestUtils.setField(correoService, "minutosRecuperacion", 30L);
		ReflectionTestUtils.setField(correoService, "diasInvitacion", 7L);
	}

	/**
	 * El correo de verificación lleva el enlace a la página de verificación del frontend.
	 *
	 * @throws Exception si no se puede leer el mensaje
	 */
	@Test
	void verificacionIncluyeEnlaceDelFrontend() throws Exception {
		User u = usuario("ana", "ana@example.com");
		assertTrue(correoService.enviarVerificacion(u, "abc-123"));
		MimeMessage m = enviado();
		assertEquals("ana@example.com", m.getAllRecipients()[0].toString());
		assertTrue(contenido(m).contains("http://localhost:4200/verificar?token=abc-123"));
	}

	/**
	 * El correo de invitación tiene el texto del requisito, los botones Sí y No, y escapa el HTML que
	 * escriben los usuarios.
	 *
	 * @throws Exception si no se puede leer el mensaje
	 */
	@Test
	void invitacionTieneTextoDelRequisitoYEscapaHtml() throws Exception {
		Salida s = new Salida();
		s.setTitulo("Empanadas <b>gratis</b>");
		s.setOrganizador(usuario("Pedro", "pedro@example.com"));
		Invitacion inv = new Invitacion();
		inv.setSalida(s);
		inv.setEmailInvitado("luis@example.com");
		inv.setToken("tok");
		assertTrue(correoService.enviarInvitacion(inv));
		MimeMessage m = enviado();
		String html = contenido(m);
		assertTrue(html.contains("¡Hey! Pedro te invitó a Empanadas &lt;b&gt;gratis&lt;/b&gt;"));
		assertTrue(html.contains("/invitacion?token=tok&respuesta=si"));
		assertTrue(html.contains("/invitacion?token=tok&respuesta=no"));
		assertEquals("Pedro te invitó a Empanadas <b>gratis</b>", m.getSubject());
	}

	/**
	 * El aviso de respuesta le llega al organizador con el nombre del invitado y si aceptó o rechazó.
	 *
	 * @throws Exception si no se puede leer el mensaje
	 */
	@Test
	void respuestaAvisaAlOrganizador() throws Exception {
		Salida s = new Salida();
		s.setTitulo("Empanadas");
		s.setOrganizador(usuario("Pedro", "pedro@example.com"));
		Invitacion inv = new Invitacion();
		inv.setSalida(s);
		inv.setEmailInvitado("luis@example.com");
		inv.setUsuarioInvitado(usuario("Luis", "luis@example.com"));
		inv.setEstado(EstadoInvitacion.RECHAZADA);
		assertTrue(correoService.enviarRespuestaInvitacion(inv));
		MimeMessage m = enviado();
		assertEquals("pedro@example.com", m.getAllRecipients()[0].toString());
		assertEquals("Luis rechazó tu invitación a Empanadas", m.getSubject());
	}

	/**
	 * Si el invitado no tiene cuenta, el aviso usa su correo y dice que aceptó.
	 *
	 * @throws Exception si no se puede leer el mensaje
	 */
	@Test
	void respuestaSinCuentaUsaElCorreoDelInvitado() throws Exception {
		Salida s = new Salida();
		s.setTitulo("Empanadas");
		s.setOrganizador(usuario("Pedro", "pedro@example.com"));
		Invitacion inv = new Invitacion();
		inv.setSalida(s);
		inv.setEmailInvitado("luis@example.com");
		inv.setEstado(EstadoInvitacion.ACEPTADA);
		assertTrue(correoService.enviarRespuestaInvitacion(inv));
		assertTrue(contenido(enviado()).contains("luis@example.com aceptó tu invitación a Empanadas"));
	}

	/**
	 * Si el servidor de correo falla, el servicio devuelve false en vez de lanzar la excepción.
	 */
	@Test
	void devuelveFalseSiFallaElEnvio() {
		doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));
		assertFalse(correoService.enviarRecuperacion(usuario("ana", "ana@example.com"), "x"));
	}

	/**
	 * Crea un usuario de prueba.
	 *
	 * @param nombre nombre de usuario
	 * @param email  correo
	 * @return usuario
	 */
	private User usuario(String nombre, String email) {
		User u = new User();
		u.setUsername(nombre);
		u.setEmail(email);
		return u;
	}

	/**
	 * Obtiene el mensaje que el servicio entregó al servidor de correo.
	 *
	 * @return mensaje enviado
	 */
	private MimeMessage enviado() {
		ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(captor.capture());
		return captor.getValue();
	}

	/**
	 * Lee el cuerpo del mensaje.
	 *
	 * @param m mensaje
	 * @return contenido como texto
	 * @throws Exception si no se puede leer
	 */
	private String contenido(MimeMessage m) throws Exception {
		m.saveChanges();
		return m.getContent().toString();
	}
}
