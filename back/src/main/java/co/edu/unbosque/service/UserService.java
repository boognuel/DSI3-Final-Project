package co.edu.unbosque.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.UserDTO;
import co.edu.unbosque.exception.PasswordNotValidException;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.UserRepository;
import co.edu.unbosque.util.PasswordValidator;

// Gestión de usuarios por el ADMIN. El registro público está en AuthService.
@Service
public class UserService implements CRUDOperation<UserDTO> {
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public long count() {
		return userRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return userRepo.existsById(id);
	}

	// 0 = ok, 1 = username en uso, 2 = correo en uso, 3 = contraseña no válida, 4 = faltan datos
	@Override
	public int create(UserDTO data) {
		if (data.getUsername() == null || data.getEmail() == null) {
			return 4;
		}
		if (userRepo.existsByUsername(data.getUsername())) {
			return 1;
		}
		String email = data.getEmail().trim().toLowerCase();
		if (userRepo.existsByEmail(email)) {
			return 2;
		}
		try {
			PasswordValidator.validar(data.getPassword());
		} catch (PasswordNotValidException e) {
			return 3;
		}
		User u = new User();
		u.setUsername(data.getUsername());
		u.setEmail(email);
		u.setPassword(passwordEncoder.encode(data.getPassword()));
		if (data.getRole() != null) {
			u.setRole(data.getRole());
		}
		u.setEmailVerificado(true); // lo crea un admin
		userRepo.save(u);
		return 0;
	}

	@Override
	public List<UserDTO> getAll() {
		List<UserDTO> dtoList = new ArrayList<>();
		userRepo.findAll().forEach(e -> dtoList.add(modelMapper.map(e, UserDTO.class)));
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		Optional<User> found = userRepo.findById(id);
		if (found.isPresent()) {
			userRepo.delete(found.get());
			return 0;
		}
		return 1;
	}

	// 0 = ok, 1 = username en uso, 2 = no existe, 3 = correo en uso, 4 = contraseña no válida
	@Override
	public int updateById(Long id, UserDTO newData) {
		Optional<User> found = userRepo.findById(id);
		if (found.isEmpty()) {
			return 2;
		}
		User u = found.get();
		if (newData.getUsername() != null && !newData.getUsername().equals(u.getUsername())) {
			if (userRepo.existsByUsername(newData.getUsername())) {
				return 1;
			}
			u.setUsername(newData.getUsername());
		}
		if (newData.getEmail() != null) {
			String email = newData.getEmail().trim().toLowerCase();
			if (!email.equals(u.getEmail())) {
				if (userRepo.existsByEmail(email)) {
					return 3;
				}
				u.setEmail(email);
			}
		}
		if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
			try {
				PasswordValidator.validar(newData.getPassword());
			} catch (PasswordNotValidException e) {
				return 4;
			}
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
		return 0;
	}

	public UserDTO getById(Long id) {
		return userRepo.findById(id).map(u -> modelMapper.map(u, UserDTO.class)).orElse(null);
	}
}
