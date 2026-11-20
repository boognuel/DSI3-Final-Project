package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.Invitacion;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {
	Optional<Invitacion> findByToken(String token);
	List<Invitacion> findBySalidaId(Long salidaId);
	List<Invitacion> findBySalidaIdAndEmailInvitadoIgnoreCase(Long salidaId, String email);
	List<Invitacion> findByEmailInvitadoIgnoreCase(String email);
	boolean existsBySalidaIdAndEmailInvitadoIgnoreCaseAndEstado(Long salidaId, String email, EstadoInvitacion estado);
	long countBySalidaIdAndEstado(Long salidaId, EstadoInvitacion estado);
}
