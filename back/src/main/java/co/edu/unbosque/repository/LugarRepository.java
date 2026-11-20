package co.edu.unbosque.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.model.Lugar;

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
	 * Lista los lugares que caben en el presupuesto, del más económico al más costoso.
	 *
	 * @param presupuesto dinero por persona
	 * @return lugares con costo estimado menor o igual al presupuesto
	 */
	@Query("select l from Lugar l where l.costoEstimado <= :presupuesto order by l.costoEstimado asc, l.nombre asc")
	List<Lugar> alcanzables(@Param("presupuesto") BigDecimal presupuesto);

	/**
	 * Lista los lugares de las categorías indicadas que caben en el presupuesto, del más económico
	 * al más costoso.
	 *
	 * @param presupuesto  dinero por persona
	 * @param categoriaIds categorías que le gustan al usuario; no debe estar vacía
	 * @return lugares de esas categorías con costo estimado menor o igual al presupuesto
	 */
	@Query("select l from Lugar l where l.costoEstimado <= :presupuesto and l.categoria.id in :categoriaIds "
			+ "order by l.costoEstimado asc, l.nombre asc")
	List<Lugar> alcanzablesEnCategorias(@Param("presupuesto") BigDecimal presupuesto,
			@Param("categoriaIds") Collection<Long> categoriaIds);

	/**
	 * Cuenta los lugares de cada categoría, incluidas las categorías sin lugares.
	 *
	 * @return filas con el nombre de la categoría y su número de lugares, en orden alfabético
	 */
	@Query("select c.nombre, count(l) from Categoria c left join Lugar l on l.categoria = c "
			+ "group by c.nombre order by c.nombre")
	List<Object[]> contarPorCategoria();

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
