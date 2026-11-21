package co.edu.unbosque.bogoparche.controller;

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
import co.edu.unbosque.bogoparche.dto.InvitacionDTO;
import co.edu.unbosque.bogoparche.exception.AccesoDenegadoException;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.service.InvitacionService;
import co.edu.unbosque.bogoparche.service.SalidaService;
import jakarta.validation.Valid;

/**
 * Invitaciones a las salidas: envío por correo, respuesta del invitado y consulta.
 */
@RestController
@RequestMapping("/invitacion")
public class InvitacionController {
	@Autowired
	private InvitacionService invitacionService;
	@Autowired
	private SalidaService salidaService;

	/**
	 * Invita a una persona a la salida enviándole un correo. Solo el organizador.
	 *
	 * @param dto  salida y correo del invitado
	 * @param auth organizador autenticado
	 * @return 201 si la invitación se envió
	 */
	@PostMapping("/enviar")
	public ResponseEntity<String> enviar(@Valid @RequestBody InvitacionDTO dto, Authentication auth) {
		invitacionService.invitar(dto, (User) auth.getPrincipal());
		return ResponseEntity.status(HttpStatus.CREATED).body("Invitación enviada");
	}

	/**
	 * Registra la respuesta del invitado. Lo llama el frontend desde la página a la que apunta el
	 * enlace del correo; no requiere sesión porque el token prueba el acceso al correo.
	 *
	 * @param token   token de la invitación
	 * @param aceptar {@code true} para aceptar, {@code false} para rechazar
	 * @return 200 con la respuesta registrada
	 */
	@PostMapping("/responder")
	public ResponseEntity<String> responder(@RequestParam String token, @RequestParam boolean aceptar) {
		invitacionService.responder(token, aceptar);
		return ResponseEntity.ok(aceptar ? "Invitación aceptada" : "Invitación rechazada");
	}

	/**
	 * Lista las invitaciones de una salida para ver quién aceptó, rechazó o sigue pendiente. Solo el
	 * organizador o un administrador.
	 *
	 * @param salidaId identificador de la salida
	 * @param auth     usuario autenticado
	 * @return 200 con la lista de invitaciones
	 */
	@GetMapping("/salida/{salidaId}")
	public ResponseEntity<List<InvitacionDTO>> porSalida(@PathVariable Long salidaId, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		salidaService.getById(salidaId);
		if (actual.getRole() != User.Role.ADMIN && !salidaService.esOrganizador(salidaId, actual.getId())) {
			throw new AccesoDenegadoException("Solo el organizador ve las invitaciones");
		}
		return ResponseEntity.ok(invitacionService.getBySalida(salidaId));
	}

	/**
	 * Lista las invitaciones recibidas en el correo del usuario autenticado.
	 *
	 * @param auth usuario autenticado
	 * @return 200 con la lista de invitaciones
	 */
	@GetMapping("/mis-invitaciones")
	public ResponseEntity<List<InvitacionDTO>> misInvitaciones(Authentication auth) {
		return ResponseEntity.ok(invitacionService.getMisInvitaciones((User) auth.getPrincipal()));
	}
}
