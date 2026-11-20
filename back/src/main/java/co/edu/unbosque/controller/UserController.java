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
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.service.AuditoriaService;
import co.edu.unbosque.service.UserService;

// Solo ADMIN (ver SecurityConfig)
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private AuditoriaService auditoriaService;

	@PostMapping("/createjson")
	public ResponseEntity<?> create(@RequestBody UserDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		int r = userService.create(dto);
		switch (r) {
		case 0:
			auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", null,
					"Creó al usuario " + dto.getUsername(), ResultadoAuditoria.EXITO);
			return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado");
		case 1:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
		case 2:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
		case 3:
			return ResponseEntity.badRequest().body("La contraseña no cumple con el estándar");
		default:
			return ResponseEntity.badRequest().body("Faltan datos obligatorios");
		}
	}

	@GetMapping("/getall")
	public ResponseEntity<List<UserDTO>> getAll() {
		List<UserDTO> lista = userService.getAll();
		return lista.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).body(lista)
				: ResponseEntity.ok(lista);
	}

	@GetMapping("/count")
	public ResponseEntity<Long> count() {
		return ResponseEntity.ok(userService.count());
	}

	@GetMapping("/exists/{id}")
	public ResponseEntity<Boolean> exists(@PathVariable Long id) {
		return ResponseEntity.ok(userService.exist(id));
	}

	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
		UserDTO dto = userService.getById(id);
		return dto == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado")
				: ResponseEntity.ok(dto);
	}

	@PutMapping("/updatejson/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		int r = userService.updateById(id, dto);
		switch (r) {
		case 0:
			auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", id, "Actualizó al usuario",
					ResultadoAuditoria.EXITO);
			return ResponseEntity.ok("Usuario actualizado");
		case 1:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
		case 2:
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
		case 3:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
		default:
			return ResponseEntity.badRequest().body("La contraseña no cumple con el estándar");
		}
	}

	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		if (actual.getId().equals(id)) {
			return ResponseEntity.badRequest().body("No puedes eliminar tu propia cuenta");
		}
		if (userService.deleteById(id) == 0) {
			auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", id, "Eliminó al usuario",
					ResultadoAuditoria.EXITO);
			return ResponseEntity.ok("Usuario eliminado");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
	}
}
