package co.edu.unbosque.bogoparche.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.bogoparche.model.SalidaLugar;

/**
 * Acceso a los lugares propuestos en cada salida.
 */
public interface SalidaLugarRepository extends JpaRepository<SalidaLugar, Long> {

	/**
	 * Lista los lugares propuestos para una salida.
	 *
	 * @param salidaId identificador de la salida
	 * @return lugares de la salida
	 */
	List<SalidaLugar> findBySalidaId(Long salidaId);

	/**
	 * Busca un lugar dentro de una salida.
	 *
	 * @param salidaId identificador de la salida
	 * @param lugarId  identificador del lugar
	 * @return el lugar de la salida, si está
	 */
	Optional<SalidaLugar> findBySalidaIdAndLugarId(Long salidaId, Long lugarId);

	/**
	 * Indica si alguna salida tiene propuesto el lugar.
	 *
	 * @param lugarId identificador del lugar
	 * @return {@code true} si el lugar está en alguna salida
	 */
	boolean existsByLugarId(Long lugarId);
}
