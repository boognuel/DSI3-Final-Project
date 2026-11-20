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

/**
 * Salidas que organizan los usuarios y los lugares que se proponen para cada una. Solo el
 * organizador o un administrador pueden modificarlas; los invitados que aceptaron pueden verlas.
 */
@RestController
@RequestMapping("/salida")
public class SalidaController {
	@Autowired
	private SalidaService salidaService;
	@Autowired
	private SalidaLugarService salidaLugarService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Indica si el usuario es administrador.
	 *
	 * @param u usuario autenticado
	 * @return {@code true} si tiene el rol ADMIN
	 */
	private boolean esAdmin(User u) {
		return u.getRole() == User.Role.ADMIN;
	}

	/**
	 * Indica si el usuario puede modificar la salida: el organizador o un administrador.
	 *
	 * @param salidaId identificador de la salida
	 * @param u        usuario autenticado
	 * @return {@code true} si puede modificarla
	 */
	private boolean puedeAdministrar(Long salidaId, User u) {
		return esAdmin(u) || salidaService.esOrganizador(salidaId, u.getId());
	}

	/**
	 * Indica si el usuario puede ver la salida: el organizador, un invitado que aceptó o un
	 * administrador.
	 *
	 * @param salidaId identificador de la salida
	 * @param u        usuario autenticado
	 * @return {@code true} si puede verla
	 */
	private boolean puedeVer(Long salidaId, User u) {
		return esAdmin(u) || salidaService.esParticipante(salidaId, u);
	}

	/**
	 * Comprueba que el usuario pueda modificar la salida.
	 *
	 * @param salidaId identificador de la salida
	 * @param u        usuario autenticado
	 * @param mensaje  mensaje de error si no tiene permiso
	 * @throws co.edu.unbosque.exception.RecursoNoEncontradoException si la salida no existe
	 * @throws AccesoDenegadoException si no es el organizador ni un administrador
	 */
	private void verificarOrganizador(Long salidaId, User u, String mensaje) {
		salidaService.getById(salidaId);
		if (!puedeAdministrar(salidaId, u)) {
			throw new AccesoDenegadoException(mensaje);
		}
	}

	/**
	 * Comprueba que el usuario pueda ver la salida y la devuelve.
	 *
	 * @param salidaId identificador de la salida
	 * @param u        usuario autenticado
	 * @return la salida
	 * @throws co.edu.unbosque.exception.RecursoNoEncontradoException si la salida no existe
	 * @throws AccesoDenegadoException si no participa en ella ni es administrador
	 */
	private SalidaDTO verificarParticipante(Long salidaId, User u) {
		SalidaDTO salida = salidaService.getById(salidaId);
		if (!puedeVer(salidaId, u)) {
			throw new AccesoDenegadoException("No participas en esta salida");
		}
		return salida;
	}

	/**
	 * Crea una salida organizada por el usuario autenticado.
	 *
	 * @param dto  datos de la salida
	 * @param auth usuario autenticado
	 * @return 201 con la salida creada
	 */
	@PostMapping("/create")
	public ResponseEntity<SalidaDTO> create(@Valid @RequestBody SalidaDTO dto, Authentication auth) {
		User actual = (User) auth.getPrincipal();
		dto.setOrganizadorId(actual.getId());
		salidaService.create(dto);
		auditoriaService.registrar(actual, AccionAuditoria.CREAR_SALIDA, "Salida", dto.getId(), dto.getTitulo(),
				ResultadoAuditoria.EXITO);
		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}

	/**
	 * Lista las salidas que organiza el usuario y aquellas en las que aceptó una invitación.
	 *
	 * @param auth usuario autenticado
	 * @return 200 con la lista de salidas
	 */
	@GetMapping("/mis-salidas")
	public ResponseEntity<List<SalidaDTO>> misSalidas(Authentication auth) {
		return ResponseEntity.ok(salidaService.getMisSalidas((User) auth.getPrincipal()));
	}

	/**
	 * Obtiene una salida en la que participa el usuario.
	 *
	 * @param id   identificador de la salida
	 * @param auth usuario autenticado
	 * @return 200 con los datos de la salida
	 */
	@GetMapping("/getbyid/{id}")
	public ResponseEntity<SalidaDTO> getById(@PathVariable Long id, Authentication auth) {
		return ResponseEntity.ok(verificarParticipante(id, (User) auth.getPrincipal()));
	}

	/**
	 * Actualiza una salida. Solo el organizador o un administrador.
	 *
	 * @param id   identificador de la salida
	 * @param dto  datos nuevos
	 * @param auth usuario autenticado
	 * @return 200 si la salida se actualizó
	 */
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

	/**
	 * Elimina una salida. Solo el organizador o un administrador.
	 *
	 * @param id   identificador de la salida
	 * @param auth usuario autenticado
	 * @return 200 si la salida se eliminó
	 */
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

	/**
	 * Agrega un lugar como opción de la salida. Solo el organizador o un administrador.
	 *
	 * @param id   identificador de la salida
	 * @param dto  lugar que se agrega
	 * @param auth usuario autenticado
	 * @return 201 si el lugar se agregó
	 */
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

	/**
	 * Lista los lugares propuestos para la salida, en el orden en que se agregaron.
	 *
	 * @param id   identificador de la salida
	 * @param auth usuario autenticado
	 * @return 200 con la lista de lugares
	 */
	@GetMapping("/{id}/lugares")
	public ResponseEntity<List<SalidaLugarDTO>> listarLugares(@PathVariable Long id, Authentication auth) {
		verificarParticipante(id, (User) auth.getPrincipal());
		return ResponseEntity.ok(salidaLugarService.listar(id));
	}

	/**
	 * Elige uno de los lugares propuestos como destino de la salida. Solo el organizador o un
	 * administrador.
	 *
	 * @param id      identificador de la salida
	 * @param lugarId identificador del lugar elegido
	 * @param auth    usuario autenticado
	 * @return 200 si el destino quedó seleccionado
	 */
	@PutMapping("/{id}/lugares/{lugarId}/seleccionar")
	public ResponseEntity<String> seleccionarLugar(@PathVariable Long id, @PathVariable Long lugarId,
			Authentication auth) {
		verificarOrganizador(id, (User) auth.getPrincipal(), "Solo el organizador puede elegir el destino");
		salidaLugarService.seleccionar(id, lugarId);
		return ResponseEntity.ok("Destino seleccionado");
	}
}
