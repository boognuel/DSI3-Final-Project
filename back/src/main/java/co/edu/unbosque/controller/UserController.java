package co.edu.unbosque.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.dto.UserDTO;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.service.AuditoriaService;
import co.edu.unbosque.service.UserService;

/**
 * Gestión de usuarios por parte del administrador. Todos los endpoints requieren el rol ADMIN (ver
 * SecurityConfig).
 */
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Crea un usuario con el correo ya verificado.
	 *
	 * @param dto  datos del usuario
	 * @param auth administrador autenticado
	 * @return 201 si el usuario se creó
	 */
	@PostMapping("/createjson")
	public ResponseEntity<?> create(@RequestBody UserDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		userService.create(dto);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", null,
				"Creó al usuario " + dto.getUsername(), ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado");
	}

	/**
	 * Lista todos los usuarios.
	 *
	 * @return 200 con la lista, o 204 si no hay usuarios
	 */
	@GetMapping("/getall")
	public ResponseEntity<List<UserDTO>> getAll() {
		List<UserDTO> lista = userService.getAll();
		return lista.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).body(lista)
				: ResponseEntity.ok(lista);
	}

	/**
	 * Cuenta los usuarios registrados.
	 *
	 * @return 200 con el número de usuarios
	 */
	@GetMapping("/count")
	public ResponseEntity<Long> count() {
		return ResponseEntity.ok(userService.count());
	}

	/**
	 * Indica si existe un usuario.
	 *
	 * @param id identificador del usuario
	 * @return 200 con {@code true} si existe
	 */
	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> exists(@PathVariable Long id) {
		return ResponseEntity.ok(userService.exist(id));
	}

	/**
	 * Obtiene un usuario por su identificador.
	 *
	 * @param id identificador del usuario
	 * @return 200 con los datos del usuario
	 */
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getById(id));
	}

	/**
	 * Actualiza los campos enviados de un usuario.
	 *
	 * @param id   identificador del usuario
	 * @param dto  datos nuevos; los campos nulos no se cambian
	 * @param auth administrador autenticado
	 * @return 200 si el usuario se actualizó
	 */
	@PutMapping("/updatejson/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		userService.updateById(id, dto);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", id, "Actualizó al usuario",
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Usuario actualizado");
	}

	/**
	 * Elimina un usuario. El administrador no puede eliminar su propia cuenta.
	 *
	 * @param id   identificador del usuario
	 * @param auth administrador autenticado
	 * @return 200 si el usuario se eliminó
	 */
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		if (actual.getId().equals(id)) {
			throw new DatosInvalidosException("No puedes eliminar tu propia cuenta");
		}
		userService.deleteById(id);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", id, "Eliminó al usuario",
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Usuario eliminado");
	}
}
