package co.edu.unbosque.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.Lugar;

/**
 * Acceso a los lugares del catálogo.
 */
public interface LugarRepository extends JpaRepository<Lugar, Long> {
	/**
	 * Busca un lugar por su identificador de OpenStreetMap.
	 *
	 * @param osmTipo tipo de elemento de OpenStreetMap
	 * @param osmId   identificador en OpenStreetMap
	 * @return el lugar, si existe
	 */
	Optional<Lugar> findByOsmTipoAndOsmId(String osmTipo, Long osmId);
}
