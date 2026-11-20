package co.edu.unbosque.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

	/**
	 * Lista todos los usuarios en el orden en que se registraron.
	 *
	 * @return usuarios ordenados por identificador
	 */
	List<User> findAllByOrderByIdAsc();

	/**
	 * Cuenta los usuarios registrados en cada mes.
	 *
	 * @return filas con año, mes y número de usuarios, de la más antigua a la más reciente
	 */
	@Query("select year(u.fechaCreacion), month(u.fechaCreacion), count(u) from User u where u.fechaCreacion is not null "
			+ "group by year(u.fechaCreacion), month(u.fechaCreacion) "
			+ "order by year(u.fechaCreacion), month(u.fechaCreacion)")
	List<Object[]> contarPorMes();
}
