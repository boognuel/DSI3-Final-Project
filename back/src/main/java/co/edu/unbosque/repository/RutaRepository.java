package co.edu.unbosque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.Ruta;

/**
 * Acceso a las rutas calculadas.
 */
public interface RutaRepository extends JpaRepository<Ruta, Long> {
}
