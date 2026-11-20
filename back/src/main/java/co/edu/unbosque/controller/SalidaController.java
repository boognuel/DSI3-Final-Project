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

	@PostMapping("/create")
	public ResponseEntity<?> create(@Valid @RequestBody SalidaDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		dto.setOrganizadorId(actual.getId());
		int r = salidaService.create(dto);
		if (r == 0) {
			auditoriaService.registrar(actual, AccionAuditoria.CREAR_SALIDA, "Salida", dto.getId(), dto.getTitulo(),
					ResultadoAuditoria.EXITO);
			return ResponseEntity.status(HttpStatus.CREATED).body(dto);
		}
		return ResponseEntity.badRequest().body("No se pudo crear la salida (revisa el presupuesto)");
	}

	@GetMapping("/mis-salidas")
	public ResponseEntity<List<SalidaDTO>> misSalidas(Authentication auth) {
		return ResponseEntity.ok(salidaService.getMisSalidas((User) auth.getPrincipal()));
	}

	@GetMapping("/getbyid/{id}")
	public ResponseEntity<?> getById(@PathVariable Long id, Authentication auth) {
		SalidaDTO dto = salidaService.getById(id);
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (!puedeVer(id, (User) auth.getPrincipal())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No participas en esta salida");
		}
		return ResponseEntity.ok(dto);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SalidaDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		if (salidaService.getById(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (!puedeAdministrar(id, actual)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el organizador puede editar la salida");
		}
		int r = salidaService.updateById(id, dto);
		if (r == 0) {
			auditoriaService.registrar(actual, AccionAuditoria.EDITAR_SALIDA, "Salida", id, dto.getTitulo(),
					ResultadoAuditoria.EXITO);
			return ResponseEntity.ok("Salida actualizada");
		}
		return ResponseEntity.badRequest().body("Presupuesto no válido");
	}

	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		if (salidaService.getById(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (!puedeAdministrar(id, actual)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el organizador puede eliminar la salida");
		}
		salidaService.deleteById(id);
		auditoriaService.registrar(actual, AccionAuditoria.ELIMINAR_SALIDA, "Salida", id, null,
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Salida eliminada");
	}

	// ---- Lugares de la salida ----

	@PostMapping("/{id}/lugares")
	public ResponseEntity<?> agregarLugar(@PathVariable Long id, @RequestBody SalidaLugarDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		if (salidaService.getById(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (!puedeAdministrar(id, actual)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el organizador puede agregar lugares");
		}
		if (dto.getLugarId() == null) {
			return ResponseEntity.badRequest().body("lugarId es obligatorio");
		}
		int r = salidaLugarService.agregar(id, dto.getLugarId());
		switch (r) {
		case 0:
			auditoriaService.registrar(actual, AccionAuditoria.AGREGAR_LUGAR, "Salida", id,
					"Lugar " + dto.getLugarId(), ResultadoAuditoria.EXITO);
			return ResponseEntity.status(HttpStatus.CREATED).body("Lugar agregado a la salida");
		case 2:
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lugar no encontrado");
		case 3:
			return ResponseEntity.status(HttpStatus.CONFLICT).body("El lugar ya está en la salida");
		case 4:
			return ResponseEntity.badRequest().body("El lugar supera el presupuesto de la salida");
		default:
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
	}

	@GetMapping("/{id}/lugares")
	public ResponseEntity<?> listarLugares(@PathVariable Long id, Authentication auth) {
		if (salidaService.getById(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (!puedeVer(id, (User) auth.getPrincipal())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No participas en esta salida");
		}
		return ResponseEntity.ok(salidaLugarService.listar(id));
	}

	@PutMapping("/{id}/lugares/{lugarId}/seleccionar")
	public ResponseEntity<?> seleccionarLugar(@PathVariable Long id, @PathVariable Long lugarId,
			Authentication auth) {
		if (salidaService.getById(id) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Salida no encontrada");
		}
		if (!puedeAdministrar(id, (User) auth.getPrincipal())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el organizador puede elegir el destino");
		}
		return salidaLugarService.seleccionar(id, lugarId) == 0 ? ResponseEntity.ok("Destino seleccionado")
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ese lugar no está en la salida");
	}
}
