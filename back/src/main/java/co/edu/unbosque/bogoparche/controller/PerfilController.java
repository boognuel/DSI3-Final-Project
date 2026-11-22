package co.edu.unbosque.bogoparche.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.bogoparche.dto.CambioPasswordDTO;
import co.edu.unbosque.bogoparche.dto.UserDTO;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.ResultadoAuditoria;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.service.AuditoriaService;
import co.edu.unbosque.bogoparche.service.UserService;

/**
 * Perfil del usuario que inició sesión: consultar sus datos, editarlos y cambiar su contraseña.
 */
@RestController
@RequestMapping("/perfil")
public class PerfilController {
	@Autowired
	private UserService userService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Consulta los datos del usuario autenticado.
	 *
	 * @param auth usuario autenticado
	 * @return 200 con sus datos
	 */
	@GetMapping("/get")
	public ResponseEntity<UserDTO> get(Authentication auth) {
		return ResponseEntity.ok(userService.getById(((User) auth.getPrincipal()).getId()));
	}

	/**
	 * Cambia el nombre de usuario o el correo del usuario autenticado.
	 *
	 * @param dto  nombre de usuario y correo nuevos
	 * @param auth usuario autenticado
	 * @return 200 si se actualizó
	 */
	@PutMapping("/updatejson")
	public ResponseEntity<String> update(@RequestBody UserDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		userService.actualizarPerfil(actual.getId(), dto);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", actual.getId(),
				"Actualizó su perfil", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Perfil actualizado");
	}

	/**
	 * Cambia la contraseña del usuario autenticado, pidiendo la actual.
	 *
	 * @param dto  contraseña actual y nueva
	 * @param auth usuario autenticado
	 * @return 200 si se cambió
	 */
	@PutMapping("/password")
	public ResponseEntity<String> cambiarPassword(@RequestBody CambioPasswordDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		userService.cambiarPassword(actual.getId(), dto);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", actual.getId(),
				"Cambió su contraseña", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Contraseña actualizada");
	}
}
