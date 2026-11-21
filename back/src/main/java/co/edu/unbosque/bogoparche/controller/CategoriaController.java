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
import co.edu.unbosque.bogoparche.dto.CategoriaDTO;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.ResultadoAuditoria;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.service.AuditoriaService;
import co.edu.unbosque.bogoparche.service.CategoriaService;
import jakarta.validation.Valid;

/**
 * Categorías de planes. Cualquier usuario con sesión puede consultarlas; crearlas, editarlas y
 * eliminarlas requiere el rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/categoria")
public class CategoriaController {
	@Autowired
	private CategoriaService categoriaService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Crea una categoría.
	 *
	 * @param dto  datos de la categoría
	 * @param auth administrador autenticado
	 * @return 201 con la categoría creada
	 */
	@PostMapping("/create")
	public ResponseEntity<CategoriaDTO> create(@Valid @RequestBody CategoriaDTO dto, Authentication auth) {
		categoriaService.create(dto);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Categoria",
				dto.getId(), "Creó la categoría " + dto.getNombre(), ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.getById(dto.getId()));
	}

	/**
	 * Lista las categorías en orden alfabético.
	 *
	 * @return 200 con la lista de categorías
	 */
	@GetMapping("/getall")
	public ResponseEntity<List<CategoriaDTO>> getAll() {
		return ResponseEntity.ok(categoriaService.getAll());
	}

	/**
	 * Obtiene una categoría por su identificador.
	 *
	 * @param id identificador de la categoría
	 * @return 200 con los datos de la categoría
	 */
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<CategoriaDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(categoriaService.getById(id));
	}

	/**
	 * Actualiza una categoría.
	 *
	 * @param id   identificador de la categoría
	 * @param dto  datos nuevos
	 * @param auth administrador autenticado
	 * @return 200 si la categoría se actualizó
	 */
	@PutMapping("/update/{id}")
	public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody CategoriaDTO dto,
			Authentication auth) {
		categoriaService.updateById(id, dto);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Categoria", id,
				"Actualizó la categoría " + dto.getNombre(), ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Categoría actualizada");
	}

	/**
	 * Elimina una categoría que no tenga lugares.
	 *
	 * @param id   identificador de la categoría
	 * @param auth administrador autenticado
	 * @return 200 si la categoría se eliminó
	 */
	@DeleteMapping("/deletebyid/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
		categoriaService.deleteById(id);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.GESTION_CATALOGO, "Categoria", id,
				"Eliminó la categoría", ResultadoAuditoria.EXITO);
		return ResponseEntity.ok("Categoría eliminada");
	}
}
