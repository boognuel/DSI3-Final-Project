package co.edu.unbosque.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import co.edu.unbosque.repository.UserRepository;

/**
 * Carga los usuarios de la base de datos para Spring Security.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	/**
	 * Crea el servicio.
	 *
	 * @param userRepository repositorio de usuarios
	 */
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Busca un usuario por su nombre de usuario.
	 *
	 * @param username nombre de usuario
	 * @return el usuario
	 * @throws UsernameNotFoundException si no existe
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
	}
}
