package co.edu.unbosque.bogoparche.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.bogoparche.dto.RegistroDTO;
import co.edu.unbosque.bogoparche.exception.ConflictoException;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.RecursoExpiradoException;
import co.edu.unbosque.bogoparche.model.TipoToken;
import co.edu.unbosque.bogoparche.model.TokenUsuario;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.TokenUsuarioRepository;
import co.edu.unbosque.bogoparche.repository.UserRepository;
import co.edu.unbosque.bogoparche.util.PasswordValidator;

/**
 * Registro público, verificación del correo y recuperación de contraseña.
 */
@Service
public class AuthService {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private TokenUsuarioRepository tokenRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private CorreoService correoService;

	@Value("${app.token.horas-verificacion}")
	private long horasVerificacion;
	@Value("${app.token.minutos-recuperacion}")
	private long minutosRecuperacion;

	/**
	 * Registra un usuario con rol USER y le envía el correo de verificación.
	 *
	 * @param dto datos del registro
	 * @return {@code true} si el correo de verificación se envió; {@code false} si la cuenta se
	 *         creó pero el correo falló
	 * @throws ConflictoException      si el nombre de usuario o el correo ya están registrados
	 * @throws DatosInvalidosException si la contraseña no cumple la política
	 */
	public boolean registrar(RegistroDTO dto) {
		if (userRepo.existsByUsername(dto.getUsername())) {
			throw new ConflictoException("El nombre de usuario ya existe");
		}
		String email = dto.getEmail().trim().toLowerCase();
		if (userRepo.existsByEmail(email)) {
			throw new ConflictoException("El correo ya está registrado");
		}
		PasswordValidator.validar(dto.getPassword());
		User u = new User();
		u.setUsername(dto.getUsername());
		u.setEmail(email);
		u.setPassword(passwordEncoder.encode(dto.getPassword()));
		u.setRole(User.Role.USER); // el registro público siempre crea USER
		u.setEmailVerificado(false);
		userRepo.save(u);
		String token = crearToken(u, TipoToken.VERIFICACION_EMAIL, LocalDateTime.now().plusHours(horasVerificacion));
		return correoService.enviarVerificacion(u, token);
	}

	/**
	 * Envía un nuevo enlace de verificación si el correo existe y no está verificado. No informa
	 * si el correo existe, para no revelar qué cuentas están registradas.
	 *
	 * @param email correo de la cuenta
	 */
	public void reenviarVerificacion(String email) {
		if (email == null) {
			return;
		}
		Optional<User> opt = userRepo.findByEmail(email.trim().toLowerCase());
		if (opt.isPresent() && !opt.get().isEmailVerificado()) {
			User u = opt.get();
			String token = crearToken(u, TipoToken.VERIFICACION_EMAIL,
					LocalDateTime.now().plusHours(horasVerificacion));
			correoService.enviarVerificacion(u, token);
		}
	}

	/**
	 * Marca el correo como verificado usando el token del enlace.
	 *
	 * @param token token recibido por correo
	 * @throws DatosInvalidosException  si el token no existe
	 * @throws RecursoExpiradoException si el token venció o ya fue usado
	 */
	@Transactional
	public void verificarEmail(String token) {
		TokenUsuario t = validarToken(token, TipoToken.VERIFICACION_EMAIL, "Enlace de verificación inválido",
				"El enlace venció o ya fue usado. Solicita uno nuevo.");
		User u = t.getUsuario();
		u.setEmailVerificado(true);
		userRepo.save(u);
		t.setUsado(true);
		tokenRepo.save(t);
	}

	/**
	 * Envía el enlace de recuperación si el correo pertenece a una cuenta activa. No informa si el
	 * correo existe, para no revelar qué cuentas están registradas.
	 *
	 * @param email correo de la cuenta
	 */
	public void solicitarRecuperacion(String email) {
		if (email == null) {
			return;
		}
		Optional<User> opt = userRepo.findByEmail(email.trim().toLowerCase());
		if (opt.isPresent() && opt.get().isEnabled()) {
			User u = opt.get();
			String token = crearToken(u, TipoToken.RECUPERACION_PASSWORD,
					LocalDateTime.now().plusMinutes(minutosRecuperacion));
			correoService.enviarRecuperacion(u, token);
		}
	}

	/**
	 * Cambia la contraseña usando el token del enlace de recuperación.
	 *
	 * @param token         token recibido por correo
	 * @param nuevaPassword contraseña nueva
	 * @throws DatosInvalidosException  si el token no existe o la contraseña no cumple la política
	 * @throws RecursoExpiradoException si el token venció o ya fue usado
	 */
	@Transactional
	public void restablecerPassword(String token, String nuevaPassword) {
		TokenUsuario t = validarToken(token, TipoToken.RECUPERACION_PASSWORD, "Enlace de recuperación inválido",
				"El enlace venció o ya fue usado");
		PasswordValidator.validar(nuevaPassword);
		User u = t.getUsuario();
		u.setPassword(passwordEncoder.encode(nuevaPassword));
		userRepo.save(u);
		t.setUsado(true);
		tokenRepo.save(t);
	}

	/**
	 * Busca un token y comprueba que no se haya usado ni haya vencido.
	 *
	 * @param token           valor del token
	 * @param tipo            tipo de token esperado
	 * @param mensajeInvalido mensaje si el token no existe
	 * @param mensajeVencido  mensaje si ya se usó o venció
	 * @return el token
	 * @throws DatosInvalidosException  si el token no existe
	 * @throws RecursoExpiradoException si ya se usó o venció
	 */
	private TokenUsuario validarToken(String token, TipoToken tipo, String mensajeInvalido, String mensajeVencido) {
		TokenUsuario t = tokenRepo.findByTokenAndTipo(token, tipo)
				.orElseThrow(() -> new DatosInvalidosException(mensajeInvalido));
		if (t.isUsado() || t.getFechaExpiracion().isBefore(LocalDateTime.now())) {
			throw new RecursoExpiradoException(mensajeVencido);
		}
		return t;
	}

	/**
	 * Crea un token nuevo e invalida los anteriores del mismo tipo que sigan sin usar.
	 *
	 * @param u      usuario
	 * @param tipo   tipo de token
	 * @param expira fecha de vencimiento
	 * @return valor del token
	 */
	private String crearToken(User u, TipoToken tipo, LocalDateTime expira) {
		// Invalida los tokens anteriores del mismo tipo que sigan sin usar
		tokenRepo.findByUsuarioAndTipoAndUsadoFalse(u, tipo).forEach(old -> {
			old.setUsado(true);
			tokenRepo.save(old);
		});
		TokenUsuario t = new TokenUsuario();
		t.setToken(UUID.randomUUID().toString());
		t.setTipo(tipo);
		t.setUsuario(u);
		t.setFechaExpiracion(expira);
		tokenRepo.save(t);
		return t.getToken();
	}
}
