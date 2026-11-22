package co.edu.unbosque.bogoparche.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.bogoparche.dto.LugarDTO;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.ResultadoAuditoria;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.service.AuditoriaService;
import co.edu.unbosque.bogoparche.service.LugarService;
import jakarta.validation.Valid;

/**
 * Lugares del catálogo que crea el equipo. Cualquier usuario con sesión puede consultarlos;
 * crearlos, editarlos y eliminarlos requiere el rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/lugar")
public class LugarController {
	@Autowired
	private LugarService lugarService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Crea un lugar.
	 *
	 * @param dto  datos del lugar
	 * @param auth administrador autenticado
	 * @return 201 con el lugar creado
	 */
	@PostMapping("/createjson")
	public ResponseEntity<LugarDTO> create(@Valid @RequestBody LugarDTO dto, Authentication auth) {
		lugarService.create(dto);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Lugar",
				dto.getId(), "Creó el lugar " + dto.getNombre(), ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body(lugarService.getById(dto.getId()));
	}

	/**
	 * Lista los lugares, del más económico al más costoso, con filtros opcionales.
	 *
	 * @param zonaId      zona a filtrar
	 * @param categoriaId categoría a filtrar
	 * @return 200 con la lista de lugares
	 */
	@GetMapping("/getall")
	public ResponseEntity<List<LugarDTO>> getAll(@RequestParam(required = false) Long zonaId,
			@RequestParam(required = false) Long categoriaId) {
		return ResponseEntity.ok(lugarService.filtrar(zonaId, categoriaId));
	}

	/**
	 * Obtiene un lugar por su identificador.
	 *
	 * @param id identificador del lugar
	 * @return 200 con los datos del lugar
	 */
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<LugarDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(lugarService.getById(id));
	}

	/**
	 * Actualiza un lugar.
	 *
	 * @param id   identificador del lugar
	 * @param dto  datos nuevos
	 * @param auth administrador autenticado
	 * @return 200 si el lugar se actualizó
	 */
	@PutMapping("/updatejson/{id}")
	public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody LugarDTO dto,
			Authentication auth) {
		lugarService.updateById(id, dto);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Lugar", id,
				"Actualizó el lugar " + dto.getNombre(), ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Lugar actualizado");
	}

	/**
	 * Elimina un lugar que no se use en salidas ni rutas.
	 *
	 * @param id   identificador del lugar
	 * @param auth administrador autenticado
	 * @return 200 si el lugar se eliminó
	 */
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
		lugarService.deleteById(id);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Lugar", id,
				"Eliminó el lugar", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Lugar eliminado");
	}
}
