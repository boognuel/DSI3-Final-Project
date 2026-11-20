package co.edu.unbosque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.dto.PasswordResetDTO;
import co.edu.unbosque.dto.RegistroDTO;
import co.edu.unbosque.dto.UserDTO;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.security.JwtUtil;
import co.edu.unbosque.service.AuditoriaService;
import co.edu.unbosque.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private AuthService authService;
	@Autowired
	private AuditoriaService auditoriaService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDTO loginRequest) {
		if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
			throw new DatosInvalidosException("Usuario y contraseña son obligatorios");
		}
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
			User user = (User) authentication.getPrincipal();
			String jwt = jwtUtil.generateToken(user);
			auditoriaService.registrar(user, AccionAuditoria.LOGIN_EXITOSO, "User", user.getId(), null,
					ResultadoAuditoria.EXITO);
			return ResponseEntity.ok(new AuthResponse(jwt, user.getRole().name()));
		} catch (DisabledException e) {
			auditoriaService.registrarSinSesion(loginRequest.getUsername(), AccionAuditoria.LOGIN_FALLIDO,
					"Cuenta sin verificar o deshabilitada", ResultadoAuditoria.FALLO);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Debes verificar tu correo antes de iniciar sesión");
		} catch (AuthenticationException e) {
			auditoriaService.registrarSinSesion(loginRequest.getUsername(), AccionAuditoria.LOGIN_FALLIDO,
					"Credenciales inválidas", ResultadoAuditoria.FALLO);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Nombre de usuario o contraseña inválidos o usuario no encontrado");
		}
	}

	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegistroDTO dto) {
		if (authService.registrar(dto)) {
			auditoriaService.registrarSinSesion(dto.getUsername(), AccionAuditoria.REGISTRO, "Cuenta creada",
					ResultadoAuditoria.EXITO);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body("Usuario registrado. Revisa tu correo para verificar la cuenta.");
		}
		auditoriaService.registrarSinSesion(dto.getUsername(), AccionAuditoria.REGISTRO,
				"Cuenta creada, falló el envío del correo", ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body("Usuario registrado, pero no pudimos enviar el correo de verificación. Solicita el reenvío.");
	}

	@GetMapping("/verify")
	public ResponseEntity<String> verify(@RequestParam String token) {
		authService.verificarEmail(token);
		auditoriaService.registrarSinSesion(null, AccionAuditoria.VERIFICAR_EMAIL, "Correo verificado",
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Correo verificado. Ya puedes iniciar sesión.");
	}

	@PostMapping("/resend-verification")
	public ResponseEntity<String> resend(@RequestBody PasswordResetDTO dto) {
		authService.reenviarVerificacion(dto.getEmail());
		return ResponseEntity.ok("Si el correo existe y no está verificado, te enviamos un nuevo enlace.");
	}

	@PostMapping("/forgot")
	public ResponseEntity<String> forgot(@RequestBody PasswordResetDTO dto) {
		authService.solicitarRecuperacion(dto.getEmail());
		auditoriaService.registrarSinSesion(null, AccionAuditoria.SOLICITAR_RECUPERACION,
				"Solicitud de recuperación de contraseña", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Si el correo existe, te enviamos un enlace para cambiar la contraseña.");
	}

	@PostMapping("/reset")
	public ResponseEntity<String> reset(@RequestBody PasswordResetDTO dto) {
		if (dto.getToken() == null || dto.getNuevaPassword() == null) {
			throw new DatosInvalidosException("Token y nueva contraseña son obligatorios");
		}
		authService.restablecerPassword(dto.getToken(), dto.getNuevaPassword());
		auditoriaService.registrarSinSesion(null, AccionAuditoria.CAMBIAR_PASSWORD,
				"Contraseña restablecida con token", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Contraseña actualizada");
	}

	private static class AuthResponse {
		private final String token;
		private final String role;

		public AuthResponse(String token, String role) {
			this.token = token;
			this.role = role;
		}

		public String getToken() {
			return token;
		}

		public String getRole() {
			return role;
		}
	}
}
