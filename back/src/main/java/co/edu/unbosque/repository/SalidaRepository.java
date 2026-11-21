package co.edu.unbosque.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.Salida;

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

	/**
	 * Lista las salidas a las que fue invitado un correo con una invitación en el estado indicado.
	 *
	 * @param email  correo del invitado
	 * @param estado estado de la invitación
	 * @return salidas sin repetir
	 */
	@Query("select distinct i.salida from Invitacion i where lower(i.emailInvitado) = lower(:email) and i.estado = :estado")
	List<Salida> findByInvitadoConEstado(@Param("email") String email, @Param("estado") EstadoInvitacion estado);
}
