package co.edu.unbosque.bogoparche.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.bogoparche.model.Categoria;

/**
 * Acceso a las categorías de planes.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
	/**
	 * Busca una categoría por nombre, sin distinguir mayúsculas.
	 *
	 * @param nombre nombre de la categoría
	 * @return la categoría, o null si no existe
	 */
	Categoria findByNombreIgnoreCase(String nombre);


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
