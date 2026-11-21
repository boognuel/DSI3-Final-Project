package co.edu.unbosque.bogoparche.service;

import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import co.edu.unbosque.bogoparche.model.EstadoInvitacion;
import co.edu.unbosque.bogoparche.model.Invitacion;
import co.edu.unbosque.bogoparche.model.Salida;
import co.edu.unbosque.bogoparche.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Envío de los correos de la aplicación con {@link JavaMailSender}: verificación de la cuenta,
 * recuperación de contraseña e invitaciones a salidas. Los enlaces apuntan a páginas del frontend,
 * que son las que llaman a la API. Cada método devuelve {@code false} si el correo no se pudo
 * enviar, para que quien lo llama decida cómo responder.
 */
@Service
public class CorreoService {
	private static final Logger log = LoggerFactory.getLogger(CorreoService.class);
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username:}")
	private String remitente;
	@Value("${app.frontend-url}")
	private String frontendUrl;
	@Value("${app.token.horas-verificacion}")
	private long horasVerificacion;
	@Value("${app.token.minutos-recuperacion}")
	private long minutosRecuperacion;
	@Value("${app.invitacion.dias-validez}")
	private long diasInvitacion;

	/**
	 * Envía el enlace para verificar el correo de una cuenta nueva.
	 *
	 * @param usuario usuario registrado
	 * @param token   token de verificación
	 * @return {@code true} si el correo se envió
	 */
	public boolean enviarVerificacion(User usuario, String token) {
		String cuerpo = "<p>Hola " + texto(usuario.getUsername()) + ",</p>"
				+ "<p>Gracias por registrarte en BogoParche. Confirma tu correo para activar la cuenta:</p>"
				+ boton(frontendUrl + "/verificar?token=" + token, "Verificar mi correo")
				+ "<p>El enlace vence en " + horasVerificacion + " horas.</p>";
		return enviar(usuario.getEmail(), "Verifica tu cuenta de BogoParche", cuerpo);
	}

	/**
	 * Envía el enlace para cambiar la contraseña.
	 *
	 * @param usuario usuario que pidió la recuperación
	 * @param token   token de recuperación
	 * @return {@code true} si el correo se envió
	 */
	public boolean enviarRecuperacion(User usuario, String token) {
		String cuerpo = "<p>Hola " + texto(usuario.getUsername()) + ",</p>"
				+ "<p>Recibimos una solicitud para cambiar la contraseña de tu cuenta de BogoParche:</p>"
				+ boton(frontendUrl + "/reset-password?token=" + token, "Cambiar mi contraseña")
				+ "<p>El enlace vence en " + minutosRecuperacion + " minutos. Si no lo pediste, ignora este correo.</p>";
		return enviar(usuario.getEmail(), "Cambia tu contraseña de BogoParche", cuerpo);
	}

	/**
	 * Envía la invitación a una salida con los enlaces para responder sí o no.
	 *
	 * @param invitacion invitación con la salida y el correo del invitado
	 * @return {@code true} si el correo se envió
	 */
	public boolean enviarInvitacion(Invitacion invitacion) {
		Salida salida = invitacion.getSalida();
		String organizador = texto(salida.getOrganizador().getUsername());
		String titulo = texto(salida.getTitulo());
		String enlace = frontendUrl + "/invitacion?token=" + invitacion.getToken();
		String cuerpo = "<p style=\"font-size:18px\"><strong>¡Hey! " + organizador + " te invitó a " + titulo
				+ "</strong></p>"
				+ (salida.getFechaHora() != null ? "<p>Fecha: " + salida.getFechaHora().format(FORMATO_FECHA) + "</p>"
						: "")
				+ "<p>¿Vas?</p>"
				+ boton(enlace + "&respuesta=si", "Sí") + "&nbsp;" + boton(enlace + "&respuesta=no", "No")
				+ "<p>La invitación vence en " + diasInvitacion + " días.</p>";
		return enviar(invitacion.getEmailInvitado(), organizador + " te invitó a " + titulo, cuerpo);
	}

	/**
	 * Avisa al organizador de una salida que un invitado aceptó o rechazó la invitación.
	 *
	 * @param invitacion invitación ya respondida, con su salida y su estado
	 * @return {@code true} si el correo se envió
	 */
	public boolean enviarRespuestaInvitacion(Invitacion invitacion) {
		Salida salida = invitacion.getSalida();
		User organizador = salida.getOrganizador();
		String invitado = texto(invitacion.getUsuarioInvitado() != null ? invitacion.getUsuarioInvitado().getUsername()
				: invitacion.getEmailInvitado());
		String titulo = texto(salida.getTitulo());
		String respuesta = invitacion.getEstado() == EstadoInvitacion.ACEPTADA ? "aceptó" : "rechazó";
		String cuerpo = "<p>Hola " + texto(organizador.getUsername()) + ",</p>"
				+ "<p style=\"font-size:18px\"><strong>" + invitado + " " + respuesta + " tu invitación a " + titulo
				+ "</strong></p>"
				+ (invitacion.getEstado() == EstadoInvitacion.ACEPTADA ? "<p>¡Ya tienes parche!</p>"
						: "<p>Puedes invitar a otras personas desde BogoParche.</p>");
		return enviar(organizador.getEmail(), invitado + " " + respuesta + " tu invitación a " + titulo, cuerpo);
	}

	/**
	 * Envía un correo HTML en UTF-8. Si el envío falla, lo registra en el log y devuelve false.
	 *
	 * @param para      destinatario
	 * @param asunto    asunto, con el texto del usuario ya escapado
	 * @param contenido cuerpo HTML sin la plantilla
	 * @return true si el correo se envió
	 */
	private boolean enviar(String para, String asunto, String contenido) {
		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
			helper.setFrom(remitente);
			helper.setTo(para);
			helper.setSubject(HtmlUtils.htmlUnescape(asunto));
			helper.setText(plantilla(contenido), true);
			mailSender.send(mensaje);
			return true;
		} catch (MessagingException | MailException e) {
			log.error("No se pudo enviar el correo \"{}\": {}", asunto, e.getMessage());
			return false;
		}
	}

	/**
	 * Escapa el texto que escriben los usuarios para que no se interprete como HTML.
	 *
	 * @param valor texto, o nulo
	 * @return texto escapado, o vacío si es nulo
	 */
	private String texto(String valor) {
		return valor == null ? "" : HtmlUtils.htmlEscape(valor);
	}

	/**
	 * Arma un enlace con estilo de botón.
	 *
	 * @param enlace   URL de destino
	 * @param etiqueta texto del botón
	 * @return HTML del botón
	 */
	private String boton(String enlace, String etiqueta) {
		return "<a href=\"" + enlace + "\" style=\"display:inline-block;padding:10px 20px;background:#e85d04;"
				+ "color:#ffffff;text-decoration:none;border-radius:6px;font-weight:bold\">" + etiqueta + "</a>";
	}

	/**
	 * Envuelve el contenido en la plantilla común de los correos, con el encabezado de BogoParche y el
	 * pie de mensaje automático.
	 *
	 * @param contenido cuerpo HTML
	 * @return correo HTML completo
	 */
	private String plantilla(String contenido) {
		return "<div style=\"font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#222\">"
				+ "<h2 style=\"color:#e85d04\">BogoParche</h2>" + contenido
				+ "<p style=\"color:#888;font-size:12px\">Este es un mensaje automático, no lo respondas.</p></div>";
	}
}
