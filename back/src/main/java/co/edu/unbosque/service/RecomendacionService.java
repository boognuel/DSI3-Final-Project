package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.RecomendacionDTO;
import co.edu.unbosque.exception.DatosInvalidosException;

/**
 * Recomienda planes según el presupuesto por persona del usuario y, si los indica, sus gustos. Una
 * zona se recomienda si su gasto mínimo cabe en el presupuesto y un lugar si su costo estimado cabe.
 */
@Service
public class RecomendacionService {
	@Autowired
	private ZonaService zonaService;
	@Autowired
	private LugarService lugarService;

	/**
	 * Busca las zonas y los lugares a los que alcanza el presupuesto.
	 *
	 * @param presupuesto  dinero por persona
	 * @param categoriaIds categorías que le gustan al usuario; nula o vacía para todas
	 * @return zonas y lugares recomendados, de los más económicos a los más costosos
	 * @throws DatosInvalidosException si el presupuesto falta o es negativo
	 */
	public RecomendacionDTO recomendar(BigDecimal presupuesto, Collection<Long> categoriaIds) {
		if (presupuesto == null || presupuesto.signum() < 0) {
			throw new DatosInvalidosException("El presupuesto es obligatorio y no puede ser negativo");
		}
		return new RecomendacionDTO(presupuesto, zonaService.alcanzables(presupuesto),
				lugarService.recomendar(presupuesto, categoriaIds));
	}
}
