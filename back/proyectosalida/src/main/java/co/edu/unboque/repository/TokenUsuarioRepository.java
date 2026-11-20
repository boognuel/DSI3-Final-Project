package co.edu.unboque.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unboque.model.TipoToken;
import co.edu.unboque.model.TokenUsuario;
import co.edu.unboque.model.User;

public interface TokenUsuarioRepository extends JpaRepository<TokenUsuario, Long> {
	Optional<TokenUsuario> findByTokenAndTipo(String token, TipoToken tipo);
	List<TokenUsuario> findByUsuarioAndTipoAndUsadoFalse(User usuario, TipoToken tipo);
}
