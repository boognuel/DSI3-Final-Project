package co.edu.unboque.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unboque.dto.InvitacionDTO;
import co.edu.unboque.model.User;
import co.edu.unboque.service.InvitacionService;
import co.edu.unboque.service.SalidaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/invitacion")
public class InvitacionController {
	@Autowired
	private InvitacionService invitacionService;
	@Autowired
	private SalidaService salidaService;

	@PostMapping("/enviar")
	public ResponseEntity<?> enviar(@Valid @RequestBody InvitacionDTO dto, Authentication auth) {
		int r = invitacionService.invitar(dto, (User) auth.getPrincipal());
		switch (r) {
		case 0:
			return ResponseEntity.status(HttpStatus.CREATED).body("Invitación enviada");
		case 1:
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		case 2:
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el organizador puede invitar");
		case 3:
			return ResponseEntity.badRequest().body("La salida está cancelada o ya se realizó");
		case 4:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Esa persona ya tiene una invitación vigente");
		case 5:
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("No se pudo enviar el correo");
		default:
			return ResponseEntity.badRequest().body("No puedes invitarte a ti mismo");
		}
	}

	// Lo llama el frontend desde la página a la que apunta el enlace del correo (no requiere sesión)
	@PostMapping("/responder")
	public ResponseEntity<?> responder(@RequestParam String token, @RequestParam boolean aceptar) {
		int r = invitacionService.responder(token, aceptar);
		switch (r) {
		case 0:
			return ResponseEntity.ok(aceptar ? "Invitación aceptada" : "Invitación rechazada");
		case 1:
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invitación no encontrada");
		case 2:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Esta invitación ya fue respondida");
		default:
			return ResponseEntity.status(HttpStatus.GONE).body("La invitación venció");
		}
	}

	// El organizador ve quién aceptó, rechazó o sigue pendiente
	@GetMapping("/salida/{salidaId}")
	public ResponseEntity<?> porSalida(@PathVariable Long salidaId, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		if (salidaService.getById(salidaId) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (actual.getRole() != User.Role.ADMIN && !salidaService.esOrganizador(salidaId, actual.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el organizador ve las invitaciones");
		}
		return ResponseEntity.ok(invitacionService.getBySalida(salidaId));
	}

	@GetMapping("/mis-invitaciones")
	public ResponseEntity<List<InvitacionDTO>> misInvitaciones(Authentication auth) {
		return ResponseEntity.ok(invitacionService.getMisInvitaciones((User) auth.getPrincipal()));
	}
}
