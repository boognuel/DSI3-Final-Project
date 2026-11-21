package co.edu.unbosque.bogoparche.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.AuditoriaLog;

/**
 * Acceso a los registros de auditoría. Extiende {@link Repository} y no JpaRepository para no
 * exponer operaciones de borrado: la auditoría solo se inserta y se consulta.
 */
public interface AuditoriaRepository extends Repository<AuditoriaLog, Long> {

	/**
	 * Guarda un registro de auditoría.
	 *
	 * @param log registro
	 * @return registro guardado con su identificador
	 */
	AuditoriaLog save(AuditoriaLog log);

	@Query("select a from AuditoriaLog a where (:usuario = '' or lower(a.usuarioNombre) like lower(concat('%', :usuario, '%'))) "
			+ "and a.fecha >= :desde and a.fecha <= :hasta")
	/**
	 * Busca registros por parte del nombre de usuario y rango de fechas.
	 *
	 * @param usuario  parte del nombre de usuario; vacío para todos
	 * @param desde    fecha inicial
	 * @param hasta    fecha final
	 * @param pageable página y orden
	 * @return página de registros
	 */
	Page<AuditoriaLog> buscar(@Param("usuario") String usuario, @Param("desde") LocalDateTime desde,
			@Param("hasta") LocalDateTime hasta, Pageable pageable);

	@Query("select a from AuditoriaLog a where (:usuario = '' or lower(a.usuarioNombre) like lower(concat('%', :usuario, '%'))) "
			+ "and a.accion = :accion and a.fecha >= :desde and a.fecha <= :hasta")
	/**
	 * Busca registros por parte del nombre de usuario, acción y rango de fechas.
	 *
	 * @param usuario  parte del nombre de usuario; vacío para todos
	 * @param accion   acción
	 * @param desde    fecha inicial
	 * @param hasta    fecha final
	 * @param pageable página y orden
	 * @return página de registros
	 */
	Page<AuditoriaLog> buscarPorAccion(@Param("usuario") String usuario, @Param("accion") AccionAuditoria accion,
			@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta, Pageable pageable);

	/**
	 * Lista los registros de un periodo en orden cronológico, para el reporte.
	 *
	 * @param desde fecha inicial
	 * @param hasta fecha final
	 * @return registros del periodo
	 */
	List<AuditoriaLog> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);

	/**
	 * Cuenta los registros de un periodo agrupados por acción.
	 *
	 * @param desde fecha inicial
	 * @param hasta fecha final
	 * @return pares [acción, cantidad]
	 */
	@Query("select a.accion, count(a) from AuditoriaLog a where a.fecha between :desde and :hasta group by a.accion")
	List<Object[]> contarPorAccion(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
