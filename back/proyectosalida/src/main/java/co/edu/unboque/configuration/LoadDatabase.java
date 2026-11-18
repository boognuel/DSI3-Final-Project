package co.edu.unboque.configuration;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import co.edu.unboque.model.User;
import co.edu.unboque.repository.UserRepository;

@Configuration
public class LoadDatabase {
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	@Bean
	CommandLineRunner initDatabase(UserRepository userRepo, PasswordEncoder passwordEncoder,
			@Value("${app.admin.email}") String adminEmail, @Value("${app.admin.password}") String adminPassword) {
		return args -> {
			Optional<User> found = userRepo.findByUsername("admin");
			if (found.isPresent()) {
				log.info("El administrador ya existe, omitiendo la creación...");
			} else if (adminPassword == null || adminPassword.isBlank()) {
				log.warn("No se creó el administrador: define la variable de entorno ADMIN_PASSWORD");
			} else {
				User admin = new User();
				admin.setUsername("admin");
				admin.setEmail(adminEmail.trim().toLowerCase());
				admin.setPassword(passwordEncoder.encode(adminPassword));
				admin.setRole(User.Role.ADMIN);
				admin.setEmailVerificado(true);
				userRepo.save(admin);
				log.info("Precargando usuario administrador");
			}
		};
	}
}
