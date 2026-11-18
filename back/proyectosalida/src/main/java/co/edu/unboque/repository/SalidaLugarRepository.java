package co.edu.unboque.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unboque.model.SalidaLugar;

public interface SalidaLugarRepository extends JpaRepository<SalidaLugar, Long> {
	List<SalidaLugar> findBySalidaId(Long salidaId);
	Optional<SalidaLugar> findBySalidaIdAndLugarId(Long salidaId, Long lugarId);
}
