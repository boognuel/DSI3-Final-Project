package co.edu.unbosque.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.Lugar;

public interface LugarRepository extends JpaRepository<Lugar, Long> {
	Optional<Lugar> findByOsmTipoAndOsmId(String osmTipo, Long osmId);
}
