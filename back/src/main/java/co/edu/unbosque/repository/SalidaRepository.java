package co.edu.unbosque.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.Salida;

public interface SalidaRepository extends JpaRepository<Salida, Long> {
	List<Salida> findByOrganizadorId(Long organizadorId);

	@Query("select distinct i.salida from Invitacion i where lower(i.emailInvitado) = lower(:email) and i.estado = :estado")
	List<Salida> findByInvitadoConEstado(@Param("email") String email, @Param("estado") EstadoInvitacion estado);
}
