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
		userService.create(dto);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", null,
				"Creó al usuario " + dto.getUsername(), ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado");
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
	public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getById(id));
	}

	@PutMapping("/updatejson/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		userService.updateById(id, dto);
		auditoriaService.registrar(actual, AccionAuditoria.GESTION_USUARIO, "User", id, "Actualizó al usuario",
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Usuario actualizado");
	}

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
