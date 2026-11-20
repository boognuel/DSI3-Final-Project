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
import co.edu.unbosque.dto.SalidaDTO;
import co.edu.unbosque.dto.SalidaLugarDTO;
import co.edu.unbosque.exception.AccesoDenegadoException;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.service.AuditoriaService;
import co.edu.unbosque.service.SalidaLugarService;
import co.edu.unbosque.service.SalidaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/salida")
public class SalidaController {
	@Autowired
	private SalidaService salidaService;
	@Autowired
	private SalidaLugarService salidaLugarService;
	@Autowired
	private AuditoriaService auditoriaService;

	private boolean esAdmin(User u) {
		return u.getRole() == User.Role.ADMIN;
	}

	private boolean puedeAdministrar(Long salidaId, User u) {
		return esAdmin(u) || salidaService.esOrganizador(salidaId, u.getId());
	}

	private boolean puedeVer(Long salidaId, User u) {
		return esAdmin(u) || salidaService.esParticipante(salidaId, u);
	}

	// Lanza 404 si la salida no existe y 403 si el usuario no la organiza ni es admin
	private void verificarOrganizador(Long salidaId, User u, String mensaje) {
		salidaService.getById(salidaId);
		if (!puedeAdministrar(salidaId, u)) {
			throw new AccesoDenegadoException(mensaje);
		}
	}

	// Lanza 404 si la salida no existe y 403 si el usuario no participa en ella ni es admin
	private SalidaDTO verificarParticipante(Long salidaId, User u) {
		SalidaDTO salida = salidaService.getById(salidaId);
		if (!puedeVer(salidaId, u)) {
			throw new AccesoDenegadoException("No participas en esta salida");
		}
		return salida;
	}

	@PostMapping("/create")
	public ResponseEntity<SalidaDTO> create(@Valid @RequestBody SalidaDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		dto.setOrganizadorId(actual.getId());
		salidaService.create(dto);
		auditoriaService.registrar(actual, AccionAuditoria.CREAR_SALIDA, "Salida", dto.getId(), dto.getTitulo(),
				ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}

	@GetMapping("/mis-salidas")
	public ResponseEntity<List<SalidaDTO>> misSalidas(Authentication auth) {
		return ResponseEntity.ok(salidaService.getMisSalidas((User) auth.getPrincipal()));
	}

	@GetMapping("/getbyid/{id}")
	public ResponseEntity<SalidaDTO> getById(@PathVariable Long id, Authentication auth) {
		return ResponseEntity.ok(verificarParticipante(id, (User) auth.getPrincipal()));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody SalidaDTO dto,
			Authentication auth) {
		User actual = (User) auth.getPrincipal();
		verificarOrganizador(id, actual, "Solo el organizador puede editar la salida");
		salidaService.updateById(id, dto);
		auditoriaService.registrar(actual, AccionAuditoria.EDITAR_SALIDA, "Salida", id, dto.getTitulo(),
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Salida actualizada");
	}

	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		verificarOrganizador(id, actual, "Solo el organizador puede eliminar la salida");
		salidaService.deleteById(id);
		auditoriaService.registrar(actual, AccionAuditoria.ELIMINAR_SALIDA, "Salida", id, null,
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Salida eliminada");
	}

	// ---- Lugares de la salida ----

	@PostMapping("/{id}/lugares")
	public ResponseEntity<String> agregarLugar(@PathVariable Long id, @RequestBody SalidaLugarDTO dto,
			Authentication auth) {
		User actual = (User) auth.getPrincipal();
		verificarOrganizador(id, actual, "Solo el organizador puede agregar lugares");
		salidaLugarService.agregar(id, dto.getLugarId());
		auditoriaService.registrar(actual, AccionAuditoria.AGREGAR_LUGAR, "Salida", id,
				"Lugar " + dto.getLugarId(), ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body("Lugar agregado a la salida");
	}

	@GetMapping("/{id}/lugares")
	public ResponseEntity<List<SalidaLugarDTO>> listarLugares(@PathVariable Long id, Authentication auth) {
		verificarParticipante(id, (User) auth.getPrincipal());
		return ResponseEntity.ok(salidaLugarService.listar(id));
	}

	@PutMapping("/{id}/lugares/{lugarId}/seleccionar")
	public ResponseEntity<String> seleccionarLugar(@PathVariable Long id, @PathVariable Long lugarId,
			Authentication auth) {
		verificarOrganizador(id, (User) auth.getPrincipal(), "Solo el organizador puede elegir el destino");
		salidaLugarService.seleccionar(id, lugarId);
		return ResponseEntity.ok("Destino seleccionado");
	}
}
