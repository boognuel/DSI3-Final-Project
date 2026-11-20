package co.edu.unbosque.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.Categoria;

/**
 * Acceso a las categorías de planes.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	/**
	 * Indica si ya existe una categoría con ese nombre, sin distinguir mayúsculas.
	 *
	 * @param nombre nombre de la categoría
	 * @return {@code true} si existe
	 */
	boolean existsByNombreIgnoreCase(String nombre);

	/**
	 * Indica si otra categoría, distinta de la indicada, ya usa ese nombre.
	 *
	 * @param nombre nombre de la categoría
	 * @param id     categoría que se está editando
	 * @return {@code true} si otra categoría lo usa
	 */
	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

	/**
	 * Lista las categorías en orden alfabético.
	 *
	 * @return categorías ordenadas por nombre
	 */
	List<Categoria> findAllByOrderByNombreAsc();
}
