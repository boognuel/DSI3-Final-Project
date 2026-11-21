package co.edu.unbosque.bogoparche.service;

import co.edu.unbosque.bogoparche.model.Invitacion;
import co.edu.unbosque.bogoparche.model.User;

/**
 * Contrato para el envío de correos. Cada método devuelve true si el correo salió.
 */
public interface CorreoService {

	/**
	 * Envía el enlace para verificar el correo de una cuenta nueva.
	 *
	 * @param usuario usuario registrado
	 * @param token   token de verificación
	 * @return {@code true} si el correo se envió
	 */
	boolean enviarVerificacion(User usuario, String token);

	/**
	 * Envía el enlace para cambiar la contraseña.
	 *
	 * @param usuario usuario que pidió la recuperación
	 * @param token   token de recuperación
	 * @return {@code true} si el correo se envió
	 */
	boolean enviarRecuperacion(User usuario, String token);

	/**
	 * Envía la invitación a una salida.
	 *
	 * @param invitacion invitación con la salida y el correo del invitado
	 * @return {@code true} si el correo se envió
	 */
	boolean enviarInvitacion(Invitacion invitacion);
}
