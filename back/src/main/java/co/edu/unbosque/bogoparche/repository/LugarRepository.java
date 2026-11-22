package co.edu.unbosque.bogoparche.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.bogoparche.model.Lugar;

/**
 * Acceso a los lugares del catálogo.
 */
public interface LugarRepository extends JpaRepository<Lugar, Long> {
	/**
	 * Busca el lugar que se cargó desde un elemento de OpenStreetMap.
	 *
	 * @param osmId tipo e identificador del elemento, por ejemplo node/123
	 * @return el lugar, o null si no se ha cargado
	 */
	Lugar findByOsmId(String osmId);

	/**
	 * Lista los lugares de una categoría que todavía tienen el precio estimado.
	 *
	 * @param categoriaId identificador de la categoría
	 * @return lugares con precio estimado
	 */
	List<Lugar> findByCategoriaIdAndPrecioEstimadoTrue(Long categoriaId);

	/**
	 * Lista todos los lugares, del más económico al más costoso.
	 *
	 * @return lugares ordenados por costo y nombre
	 */
	List<Lugar> findAllByOrderByCostoEstimadoAscNombreAsc();

	/**
	 * Lista los lugares que caben en el presupuesto, del más económico al más costoso.
	 *
	 * @param presupuesto dinero por persona
	 * @return lugares con costo estimado menor o igual al presupuesto
	 */
	List<Lugar> findByCostoEstimadoLessThanEqualOrderByCostoEstimadoAscNombreAsc(BigDecimal presupuesto);

	/**
	 * Lista los lugares de las categorías indicadas que caben en el presupuesto, del más económico
	 * al más costoso.
	 *
	 * @param presupuesto  dinero por persona
	 * @param categoriaIds categorías que le gustan al usuario; no debe estar vacía
	 * @return lugares de esas categorías con costo estimado menor o igual al presupuesto
	 */
	List<Lugar> findByCostoEstimadoLessThanEqualAndCategoriaIdInOrderByCostoEstimadoAscNombreAsc(
			BigDecimal presupuesto, Collection<Long> categoriaIds);

	/**
	 * Cuenta los lugares de una categoría.
	 *
	 * @param categoriaId identificador de la categoría
	 * @return número de lugares
	 */
	long countByCategoriaId(Long categoriaId);

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
