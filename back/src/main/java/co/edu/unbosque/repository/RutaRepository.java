package co.edu.unbosque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.Ruta;

/**
 * Acceso a las rutas calculadas.
 */
public interface RutaRepository extends JpaRepository<Ruta, Long> {

	/**
	 * Indica si hay rutas calculadas hacia el lugar.
	 *
	 * @param lugarId identificador del lugar
	 * @return {@code true} si alguna ruta llega al lugar
	 */
	boolean existsByLugarDestinoId(Long lugarId);
}
