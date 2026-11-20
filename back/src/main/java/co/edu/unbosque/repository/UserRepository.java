package co.edu.unbosque.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.model.User;

/**
 * Acceso a los usuarios.
 */
public interface UserRepository extends JpaRepository<User, Long> {
	/**
	 * Busca un usuario por su nombre de usuario.
	 *
	 * @param username nombre de usuario
	 * @return el usuario, si existe
	 */
	Optional<User> findByUsername(String username);
	/**
	 * Busca un usuario por su correo.
	 *
	 * @param email correo
	 * @return el usuario, si existe
	 */
	Optional<User> findByEmail(String email);
	/**
	 * Indica si ya existe un usuario con ese nombre.
	 *
	 * @param username nombre de usuario
	 * @return {@code true} si existe
	 */
	boolean existsByUsername(String username);
	/**
	 * Indica si ya existe un usuario con ese correo.
	 *
	 * @param email correo
	 * @return {@code true} si existe
	 */
	boolean existsByEmail(String email);
}
