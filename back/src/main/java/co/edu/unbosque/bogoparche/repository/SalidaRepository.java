package co.edu.unbosque.bogoparche.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.bogoparche.model.Salida;

/**
 * Acceso a las salidas.
 */
public interface SalidaRepository extends JpaRepository<Salida, Long> {
	/**
	 * Lista las salidas que organiza un usuario.
	 *
	 * @param organizadorId identificador del organizador
	 * @return salidas del organizador
	 */
	List<Salida> findByOrganizadorId(Long organizadorId);
}
