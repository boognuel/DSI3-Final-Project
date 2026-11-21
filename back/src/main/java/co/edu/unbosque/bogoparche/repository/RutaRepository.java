package co.edu.unbosque.bogoparche.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.bogoparche.model.Ruta;

/**
 * Acceso a las rutas calculadas.
 */
public interface RutaRepository extends JpaRepository<Ruta, Long> {
}
