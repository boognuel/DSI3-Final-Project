package co.edu.unbosque.bogoparche.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.bogoparche.model.TipoToken;
import co.edu.unbosque.bogoparche.model.TokenUsuario;
import co.edu.unbosque.bogoparche.model.User;

/**
 * Acceso a los tokens de verificación y recuperación.
 */
public interface TokenUsuarioRepository extends JpaRepository<TokenUsuario, Long> {
	/**
	 * Busca un token de un tipo.
	 *
	 * @param token valor del token
	 * @param tipo  tipo de token
	 * @return el token, si existe
	 */
	Optional<TokenUsuario> findByTokenAndTipo(String token, TipoToken tipo);
	/**
	 * Lista los tokens de un tipo que el usuario todavía no ha usado.
	 *
	 * @param usuario usuario
	 * @param tipo    tipo de token
	 * @return tokens sin usar
	 */
	List<TokenUsuario> findByUsuarioAndTipoAndUsadoFalse(User usuario, TipoToken tipo);
}
