package co.edu.unbosque.bogoparche.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.bogoparche.model.Lugar;

/**
 * Acceso a los lugares del catálogo.
 */
public interface LugarRepository extends JpaRepository<Lugar, Long> {

	/**
	 * Lista los lugares con filtros opcionales, del más económico al más costoso.
	 *
	 * @param zonaId      zona; nula para todas
	 * @param categoriaId categoría; nula para todas
	 * @return lugares que cumplen los filtros
	 */
	@Query("select l from Lugar l where (:zonaId is null or l.zona.id = :zonaId) "
			+ "and (:categoriaId is null or l.categoria.id = :categoriaId) order by l.costoEstimado asc, l.nombre asc")
	List<Lugar> buscar(@Param("zonaId") Long zonaId, @Param("categoriaId") Long categoriaId);

	/**
	 * Indica si hay lugares en la zona.
	 *
	 * @param zonaId identificador de la zona
	 * @return {@code true} si la zona tiene lugares
	 */
	boolean existsByZonaId(Long zonaId);

	/**
	 * Indica si hay lugares en la categoría.
	 *
	 * @param categoriaId identificador de la categoría
	 * @return {@code true} si la categoría tiene lugares
	 */
	boolean existsByCategoriaId(Long categoriaId);

	/**
	 * Indica si algún lugar de la zona cuesta menos que el valor indicado.
	 *
	 * @param zonaId identificador de la zona
	 * @param minimo gasto mínimo de la zona
	 * @return {@code true} si algún lugar cuesta menos
	 */
	boolean existsByZonaIdAndCostoEstimadoLessThan(Long zonaId, BigDecimal minimo);

	/**
	 * Indica si algún lugar de la zona cuesta más que el valor indicado.
	 *
	 * @param zonaId identificador de la zona
	 * @param maximo gasto máximo de la zona
	 * @return {@code true} si algún lugar cuesta más
	 */
	boolean existsByZonaIdAndCostoEstimadoGreaterThan(Long zonaId, BigDecimal maximo);
}
