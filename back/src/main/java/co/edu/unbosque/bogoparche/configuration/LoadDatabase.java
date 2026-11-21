package co.edu.unbosque.bogoparche.configuration;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import co.edu.unbosque.bogoparche.model.Categoria;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.CategoriaRepository;
import co.edu.unbosque.bogoparche.repository.UserRepository;

/**
 * Carga los datos iniciales de la base de datos al arrancar la aplicación: el usuario
 * administrador y las categorías básicas de planes, si todavía no existen.
 */
@Configuration
public class LoadDatabase {
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	/**
	 * Crea el usuario administrador con el correo y la contraseña configurados. Si la variable de
	 * entorno {@code ADMIN_PASSWORD} no está definida, no lo crea y lo informa en el log.
	 *
	 * @param userRepo        repositorio de usuarios
	 * @param passwordEncoder codificador para guardar la contraseña cifrada
	 * @param adminEmail      correo del administrador ({@code app.admin.email})
	 * @param adminPassword   contraseña del administrador ({@code app.admin.password})
	 * @return tarea que se ejecuta al iniciar la aplicación
	 */
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

	/** Categorías que pide el requisito; el administrador puede agregar más. */
	private static final Map<String, String> CATEGORIAS = Map.of("bailar", "Discotecas y sitios para bailar",
			"comer", "Restaurantes, panaderías y comidas rápidas", "tomar", "Bares, cafés y sitios para tomar algo",
			"jugar", "Videojuegos, bolos, billar y juegos de mesa");

	/**
	 * Crea las categorías básicas de planes que todavía no existan.
	 *
	 * @param categoriaRepo repositorio de categorías
	 * @return tarea que se ejecuta al iniciar la aplicación
	 */
	@Bean
	CommandLineRunner initCategorias(CategoriaRepository categoriaRepo) {
		return args -> CATEGORIAS.forEach((nombre, descripcion) -> {
			if (!categoriaRepo.existsByNombreIgnoreCase(nombre)) {
				Categoria c = new Categoria();
				c.setNombre(nombre);
				c.setDescripcion(descripcion);
				categoriaRepo.save(c);
				log.info("Precargando categoría {}", nombre);
			}
		});
	}
}
