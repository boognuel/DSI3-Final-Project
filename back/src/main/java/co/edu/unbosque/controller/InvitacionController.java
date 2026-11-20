package co.edu.unbosque.controller;

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
import co.edu.unbosque.dto.InvitacionDTO;
import co.edu.unbosque.exception.AccesoDenegadoException;
import co.edu.unbosque.model.User;
import co.edu.unbosque.service.InvitacionService;
import co.edu.unbosque.service.SalidaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/invitacion")
public class InvitacionController {
	@Autowired
	private InvitacionService invitacionService;
	@Autowired
	private SalidaService salidaService;

	@PostMapping("/enviar")
	public ResponseEntity<String> enviar(@Valid @RequestBody InvitacionDTO dto, Authentication auth) {
		invitacionService.invitar(dto, (User) auth.getPrincipal());
		return ResponseEntity.status(HttpStatus.CREATED).body("Invitación enviada");
	}

	// Lo llama el frontend desde la página a la que apunta el enlace del correo (no requiere sesión)
	@PostMapping("/responder")
	public ResponseEntity<String> responder(@RequestParam String token, @RequestParam boolean aceptar) {
		invitacionService.responder(token, aceptar);
		return ResponseEntity.ok(aceptar ? "Invitación aceptada" : "Invitación rechazada");
	}

	// El organizador ve quién aceptó, rechazó o sigue pendiente
	@GetMapping("/salida/{salidaId}")
	public ResponseEntity<List<InvitacionDTO>> porSalida(@PathVariable Long salidaId, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		salidaService.getById(salidaId);
		if (actual.getRole() != User.Role.ADMIN && !salidaService.esOrganizador(salidaId, actual.getId())) {
			throw new AccesoDenegadoException("Solo el organizador ve las invitaciones");
		}
		return ResponseEntity.ok(invitacionService.getBySalida(salidaId));
	}

	@GetMapping("/mis-invitaciones")
	public ResponseEntity<List<InvitacionDTO>> misInvitaciones(Authentication auth) {
		return ResponseEntity.ok(invitacionService.getMisInvitaciones((User) auth.getPrincipal()));
	}
}
