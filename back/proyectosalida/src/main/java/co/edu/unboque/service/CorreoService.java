package co.edu.unboque.service;

import co.edu.unboque.model.Invitacion;
import co.edu.unboque.model.User;

// Contrato para el envío de correos. La implementación (Java Mail) la hace quien trabaja las integraciones:
// debe ser una clase @Service que implemente esta interfaz. Cada método devuelve true si el correo salió.
public interface CorreoService {

	// Enlace sugerido: {app.base-url}/auth/verify?token={token}
	boolean enviarVerificacion(User usuario, String token);

	// Enlace sugerido: {app.frontend-url}/reset-password?token={token}
	boolean enviarRecuperacion(User usuario, String token);

	// Enlace sugerido: {app.frontend-url}/invitacion?token={invitacion.getToken()}
	boolean enviarInvitacion(Invitacion invitacion);
}
