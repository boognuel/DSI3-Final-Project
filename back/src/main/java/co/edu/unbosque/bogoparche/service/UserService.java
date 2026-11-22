package co.edu.unbosque.bogoparche.service;

import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.CambioPasswordDTO;
import co.edu.unbosque.bogoparche.dto.UserDTO;
import co.edu.unbosque.bogoparche.exception.ConflictoException;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.RecursoNoEncontradoException;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.UserRepository;
import co.edu.unbosque.bogoparche.util.PasswordValidator;

/**
 * Gestión de usuarios por parte del administrador. El registro público está en
 * {@link AuthService}.
 */
@Service
public class UserService implements CRUDOperation<UserDTO> {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Cuenta los usuarios.
	 *
	 * @return número de usuarios
	 */
	@Override
	public long count() {
		return userRepo.count();
	}

	/**
	 * Indica si existe un usuario.
	 *
	 * @param id identificador del usuario
	 * @return {@code true} si existe
	 */
	@Override
	public boolean exist(Long id) {
		return userRepo.existsById(id);
	}

	/**
	 * Crea un usuario con el correo ya verificado, porque lo registra un administrador.
	 *
	 * @param data datos del usuario
	 * @throws DatosInvalidosException si faltan el usuario o el correo, o la contraseña no cumple
	 *                                 la política
	 * @throws ConflictoException      si el nombre de usuario o el correo ya están registrados
	 */
	@Override
	public void create(UserDTO data) {
		if (data.getUsername() == null || data.getEmail() == null) {
			throw new DatosInvalidosException("Faltan datos obligatorios");
		}
		if (userRepo.existsByUsername(data.getUsername())) {
			throw new ConflictoException("El nombre de usuario ya existe");
		}
		String email = data.getEmail().trim().toLowerCase();
		if (userRepo.existsByEmail(email)) {
			throw new ConflictoException("El correo ya está registrado");
		}
		PasswordValidator.validar(data.getPassword());
		User u = new User();
		u.setUsername(data.getUsername());
		u.setEmail(email);
		u.setPassword(passwordEncoder.encode(data.getPassword()));
		if (data.getRole() != null) {
			u.setRole(data.getRole());
		}
		u.setEmailVerificado(true);
		userRepo.save(u);
	}

	/**
	 * Lista todos los usuarios, sin sus contraseñas.
	 *
	 * @return usuarios
	 */
	@Override
	public List<UserDTO> getAll() {
		List<UserDTO> dtoList = new ArrayList<>();
		userRepo.findAll().forEach(e -> dtoList.add(modelMapper.map(e, UserDTO.class)));
		return dtoList;
	}

	/**
	 * Elimina un usuario.
	 *
	 * @param id identificador del usuario
	 * @throws RecursoNoEncontradoException si el usuario no existe
	 */
	@Override
	public void deleteById(Long id) {
		userRepo.delete(buscar(id));
	}

	/**
	 * Actualiza los campos enviados del usuario; los campos nulos se dejan igual.
	 *
	 * @param id      identificador del usuario
	 * @param newData datos nuevos
	 * @throws RecursoNoEncontradoException si el usuario no existe
	 * @throws ConflictoException           si el nuevo nombre de usuario o correo ya están en uso
	 * @throws DatosInvalidosException      si la nueva contraseña no cumple la política
	 */
	@Override
	public void updateById(Long id, UserDTO newData) {
		User u = buscar(id);
		if (newData.getUsername() != null && !newData.getUsername().equals(u.getUsername())) {
			if (userRepo.existsByUsername(newData.getUsername())) {
				throw new ConflictoException("El nombre de usuario ya existe");
			}
			u.setUsername(newData.getUsername());
		}
		if (newData.getEmail() != null) {
			String email = newData.getEmail().trim().toLowerCase();
			if (!email.equals(u.getEmail())) {
				if (userRepo.existsByEmail(email)) {
					throw new ConflictoException("El correo ya está registrado");
				}
				u.setEmail(email);
			}
		}
		if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
			PasswordValidator.validar(newData.getPassword());
			u.setPassword(passwordEncoder.encode(newData.getPassword()));
		}
		if (newData.getRole() != null) {
			u.setRole(newData.getRole());
		}
		if (newData.getEnabled() != null) {
			u.setEnabled(newData.getEnabled());
		}
		if (newData.getEmailVerificado() != null) {
			u.setEmailVerificado(newData.getEmailVerificado());
		}
		userRepo.save(u);
	}

	/**
	 * Obtiene un usuario por su identificador.
	 *
	 * @param id identificador del usuario
	 * @return datos del usuario
	 * @throws RecursoNoEncontradoException si el usuario no existe
	 */
	public UserDTO getById(Long id) {
		return modelMapper.map(buscar(id), UserDTO.class);
	}

	/**
	 * Cambia el nombre de usuario o el correo de un usuario desde su perfil. El rol y el estado de la
	 * cuenta no se pueden cambiar por esta vía.
	 *
	 * @param id   identificador del usuario
	 * @param data nombre de usuario y correo nuevos
	 * @throws DatosInvalidosException      si el nombre o el correo vienen vacíos
	 * @throws ConflictoException           si otro usuario ya los usa
	 * @throws RecursoNoEncontradoException si el usuario no existe
	 */
	public void actualizarPerfil(Long id, UserDTO data) {
		if (data.getUsername() == null || data.getUsername().isBlank() || data.getEmail() == null
				|| data.getEmail().isBlank()) {
			throw new DatosInvalidosException("El nombre de usuario y el correo son obligatorios");
		}
		if (!data.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new DatosInvalidosException("El correo no es válido");
		}
		UserDTO cambios = new UserDTO();
		cambios.setUsername(data.getUsername().trim());
		cambios.setEmail(data.getEmail());
		updateById(id, cambios);
	}

	/**
	 * Cambia la contraseña de un usuario después de comprobar la actual.
	 *
	 * @param id   identificador del usuario
	 * @param data contraseña actual y nueva
	 * @throws DatosInvalidosException      si falta alguna o la actual no coincide
	 * @throws DatosInvalidosException      si la nueva no cumple el estándar de contraseña
	 * @throws RecursoNoEncontradoException si el usuario no existe
	 */
	public void cambiarPassword(Long id, CambioPasswordDTO data) {
		if (data.getActual() == null || data.getActual().isBlank() || data.getNueva() == null
				|| data.getNueva().isBlank()) {
			throw new DatosInvalidosException("La contraseña actual y la nueva son obligatorias");
		}
		User u = buscar(id);
		if (!passwordEncoder.matches(data.getActual(), u.getPassword())) {
			throw new DatosInvalidosException("La contraseña actual no es correcta");
		}
		PasswordValidator.validar(data.getNueva());
		u.setPassword(passwordEncoder.encode(data.getNueva()));
		userRepo.save(u);
	}

	/**
	 * Busca la entidad de un usuario.
	 *
	 * @param id identificador del usuario
	 * @return el usuario
	 * @throws RecursoNoEncontradoException si no existe
	 */
	private User buscar(Long id) {
		return userRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
	}
}
