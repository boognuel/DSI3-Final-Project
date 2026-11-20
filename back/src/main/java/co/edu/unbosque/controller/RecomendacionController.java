package co.edu.unbosque.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.dto.RecomendacionDTO;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.service.AuditoriaService;
import co.edu.unbosque.service.RecomendacionService;

/**
 * Recomendaciones de planes según el presupuesto y los gustos del usuario. Requiere sesión.
 */
@RestController
@RequestMapping("/recomendacion")
public class RecomendacionController {
	@Autowired
	private RecomendacionService recomendacionService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Recomienda las zonas y los lugares a los que alcanza el presupuesto, de los más económicos a los
	 * más costosos. Las categorías se envían repitiendo el parámetro, por ejemplo
	 * {@code ?presupuesto=5000&categoriaIds=1&categoriaIds=2}.
	 *
	 * @param presupuesto  dinero por persona, en pesos colombianos
	 * @param categoriaIds categorías que le gustan al usuario; si se omiten se incluyen todas
	 * @param auth         usuario autenticado
	 * @return 200 con las zonas y los lugares recomendados
	 */
	@GetMapping
	public ResponseEntity<RecomendacionDTO> recomendar(@RequestParam BigDecimal presupuesto,
			@RequestParam(required = false) List<Long> categoriaIds, Authentication auth) {
		RecomendacionDTO recomendacion = recomendacionService.recomendar(presupuesto, categoriaIds);
		auditoriaService.registrar((User) auth.getPrincipal(), AccionAuditoria.BUSCAR_LUGARES, "Lugar", null,
				"Buscó planes con presupuesto " + presupuesto.toPlainString()
						+ (categoriaIds == null || categoriaIds.isEmpty() ? "" : " en las categorías " + categoriaIds)
						+ ": " + recomendacion.getLugares().size() + " lugares",
				ResultadoAuditoria.EXITO);
		return ResponseEntity.ok(recomendacion);
	}
}
