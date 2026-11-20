package co.edu.unbosque.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.AuditoriaLog;

/**
 * Acceso a los registros de auditoría. Extiende {@link Repository} y no JpaRepository para no
 * exponer operaciones de borrado: la auditoría solo se inserta y se consulta.
 */
public interface AuditoriaRepository extends Repository<AuditoriaLog, Long> {

	AuditoriaLog save(AuditoriaLog log);

	@Query("select a from AuditoriaLog a where (:usuario = '' or lower(a.usuarioNombre) like lower(concat('%', :usuario, '%'))) "
			+ "and a.fecha >= :desde and a.fecha <= :hasta")
	Page<AuditoriaLog> buscar(@Param("usuario") String usuario, @Param("desde") LocalDateTime desde,
			@Param("hasta") LocalDateTime hasta, Pageable pageable);

	@Query("select a from AuditoriaLog a where (:usuario = '' or lower(a.usuarioNombre) like lower(concat('%', :usuario, '%'))) "
			+ "and a.accion = :accion and a.fecha >= :desde and a.fecha <= :hasta")
	Page<AuditoriaLog> buscarPorAccion(@Param("usuario") String usuario, @Param("accion") AccionAuditoria accion,
			@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta, Pageable pageable);

	List<AuditoriaLog> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);

	@Query("select a.accion, count(a) from AuditoriaLog a where a.fecha between :desde and :hasta group by a.accion")
	List<Object[]> contarPorAccion(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
