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
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.bogoparche.dto.ZonaDTO;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.ResultadoAuditoria;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.service.AuditoriaService;
import co.edu.unbosque.bogoparche.service.ZonaService;
import jakarta.validation.Valid;

/**
 * Zonas de la ciudad con su rango de gasto. Cualquier usuario con sesión puede consultarlas;
 * crearlas, editarlas y eliminarlas requiere el rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/zona")
public class ZonaController {
	@Autowired
	private ZonaService zonaService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Crea una zona.
	 *
	 * @param dto  datos de la zona
	 * @param auth administrador autenticado
	 * @return 201 con la zona creada
	 */
	@PostMapping("/createjson")
	public ResponseEntity<ZonaDTO> create(@Valid @RequestBody ZonaDTO dto, Authentication auth) {
		zonaService.create(dto);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Zona", dto.getId(),
				"Creó la zona " + dto.getNombre(), ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.getById(dto.getId()));
	}

	/**
	 * Lista las zonas en orden alfabético.
	 *
	 * @return 200 con la lista de zonas
	 */
	@GetMapping("/getall")
	public ResponseEntity<List<ZonaDTO>> getAll() {
		return ResponseEntity.ok(zonaService.getAll());
	}

	/**
	 * Obtiene una zona por su identificador.
	 *
	 * @param id identificador de la zona
	 * @return 200 con los datos de la zona
	 */
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<ZonaDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(zonaService.getById(id));
	}

	/**
	 * Actualiza una zona. El rango nuevo debe cubrir el costo de los lugares que ya tiene.
	 *
	 * @param id   identificador de la zona
	 * @param dto  datos nuevos
	 * @param auth administrador autenticado
	 * @return 200 si la zona se actualizó
	 */
	@PutMapping("/updatejson/{id}")
	public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody ZonaDTO dto, Authentication auth) {
		zonaService.updateById(id, dto);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Zona", id,
				"Actualizó la zona " + dto.getNombre(), ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Zona actualizada");
	}

	/**
	 * Elimina una zona que no tenga lugares.
	 *
	 * @param id   identificador de la zona
	 * @param auth administrador autenticado
	 * @return 200 si la zona se eliminó
	 */
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
		zonaService.deleteById(id);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Zona", id,
				"Eliminó la zona", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Zona eliminada");
	}
}
