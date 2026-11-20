package co.edu.unbosque.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.Zona;

/**
 * Acceso a las zonas de la ciudad.
 */
public interface ZonaRepository extends JpaRepository<Zona, Long> {

	/**
	 * Indica si ya existe una zona con ese nombre, sin distinguir mayúsculas.
	 *
	 * @param nombre nombre de la zona
	 * @return {@code true} si existe
	 */
	boolean existsByNombreIgnoreCase(String nombre);

	/**
	 * Indica si otra zona, distinta de la indicada, ya usa ese nombre.
	 *
	 * @param nombre nombre de la zona
	 * @param id     zona que se está editando
	 * @return {@code true} si otra zona lo usa
	 */
	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

	/**
	 * Lista las zonas en orden alfabético.
	 *
	 * @return zonas ordenadas por nombre
	 */
	List<Zona> findAllByOrderByNombreAsc();
}
