package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.Invitacion;

/**
 * Acceso a las invitaciones a las salidas.
 */
public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {
	/**
	 * Busca una invitación por el token del enlace del correo.
	 *
	 * @param token token de la invitación
	 * @return la invitación, si existe
	 */
	Optional<Invitacion> findByToken(String token);
	/**
	 * Lista las invitaciones de una salida.
	 *
	 * @param salidaId identificador de la salida
	 * @return invitaciones de la salida
	 */
	List<Invitacion> findBySalidaId(Long salidaId);
	/**
	 * Lista las invitaciones de una salida enviadas a un correo, sin distinguir mayúsculas.
	 *
	 * @param salidaId identificador de la salida
	 * @param email    correo del invitado
	 * @return invitaciones de ese correo en la salida
	 */
	List<Invitacion> findBySalidaIdAndEmailInvitadoIgnoreCase(Long salidaId, String email);
	/**
	 * Lista las invitaciones enviadas a un correo, sin distinguir mayúsculas.
	 *
	 * @param email correo del invitado
	 * @return invitaciones recibidas
	 */
	List<Invitacion> findByEmailInvitadoIgnoreCase(String email);
	/**
	 * Indica si un correo tiene una invitación en ese estado para la salida.
	 *
	 * @param salidaId identificador de la salida
	 * @param email    correo del invitado
	 * @param estado   estado de la invitación
	 * @return {@code true} si existe
	 */
	boolean existsBySalidaIdAndEmailInvitadoIgnoreCaseAndEstado(Long salidaId, String email, EstadoInvitacion estado);
	/**
	 * Cuenta las invitaciones de una salida en un estado.
	 *
	 * @param salidaId identificador de la salida
	 * @param estado   estado de la invitación
	 * @return número de invitaciones
	 */
	long countBySalidaIdAndEstado(Long salidaId, EstadoInvitacion estado);
}
