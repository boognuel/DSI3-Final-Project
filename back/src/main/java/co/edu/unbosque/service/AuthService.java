package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.dto.RegistroDTO;
import co.edu.unbosque.exception.PasswordNotValidException;
import co.edu.unbosque.model.TipoToken;
import co.edu.unbosque.model.TokenUsuario;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.TokenUsuarioRepository;
import co.edu.unbosque.repository.UserRepository;
import co.edu.unbosque.util.PasswordValidator;

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

	// 0 = ok, 1 = username en uso, 2 = correo en uso, 3 = creado pero falló el envío del correo
	public int registrar(RegistroDTO dto) throws PasswordNotValidException {
		if (userRepo.existsByUsername(dto.getUsername())) {
			return 1;
		}
		String email = dto.getEmail().trim().toLowerCase();
		if (userRepo.existsByEmail(email)) {
			return 2;
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
		return correoService.enviarVerificacion(u, token) ? 0 : 3;
	}

	// Siempre 0 para no revelar si el correo existe
	public int reenviarVerificacion(String email) {
		if (email == null) {
			return 0;
		}
		Optional<User> opt = userRepo.findByEmail(email.trim().toLowerCase());
		if (opt.isPresent() && !opt.get().isEmailVerificado()) {
			User u = opt.get();
			String token = crearToken(u, TipoToken.VERIFICACION_EMAIL,
					LocalDateTime.now().plusHours(horasVerificacion));
			correoService.enviarVerificacion(u, token);
		}
		return 0;
	}

	// 0 = ok, 1 = token inválido, 2 = vencido o ya usado
	@Transactional
	public int verificarEmail(String token) {
		Optional<TokenUsuario> opt = tokenRepo.findByTokenAndTipo(token, TipoToken.VERIFICACION_EMAIL);
		if (opt.isEmpty()) {
			return 1;
		}
		TokenUsuario t = opt.get();
		if (t.isUsado() || t.getFechaExpiracion().isBefore(LocalDateTime.now())) {
			return 2;
		}
		User u = t.getUsuario();
		u.setEmailVerificado(true);
		userRepo.save(u);
		t.setUsado(true);
		tokenRepo.save(t);
		return 0;
	}

	// Siempre 0 para no revelar si el correo existe
	public int solicitarRecuperacion(String email) {
		if (email == null) {
			return 0;
		}
		Optional<User> opt = userRepo.findByEmail(email.trim().toLowerCase());
		if (opt.isPresent() && opt.get().isEnabled()) {
			User u = opt.get();
			String token = crearToken(u, TipoToken.RECUPERACION_PASSWORD,
					LocalDateTime.now().plusMinutes(minutosRecuperacion));
			correoService.enviarRecuperacion(u, token);
		}
		return 0;
	}

	// 0 = ok, 1 = token inválido, 2 = vencido o ya usado
	@Transactional
	public int restablecerPassword(String token, String nuevaPassword) throws PasswordNotValidException {
		Optional<TokenUsuario> opt = tokenRepo.findByTokenAndTipo(token, TipoToken.RECUPERACION_PASSWORD);
		if (opt.isEmpty()) {
			return 1;
		}
		TokenUsuario t = opt.get();
		if (t.isUsado() || t.getFechaExpiracion().isBefore(LocalDateTime.now())) {
			return 2;
		}
		PasswordValidator.validar(nuevaPassword);
		User u = t.getUsuario();
		u.setPassword(passwordEncoder.encode(nuevaPassword));
		userRepo.save(u);
		t.setUsado(true);
		tokenRepo.save(t);
		return 0;
	}

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
