package co.edu.unbosque.bogoparche.configuration;

import java.math.BigDecimal;
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
import co.edu.unbosque.bogoparche.model.Zona;
import co.edu.unbosque.bogoparche.repository.CategoriaRepository;
import co.edu.unbosque.bogoparche.repository.UserRepository;
import co.edu.unbosque.bogoparche.repository.ZonaRepository;

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

	/**
	 * Categorías que pide el requisito: nombre, descripción, etiquetas de OpenStreetMap y precio de
	 * referencia por persona. El administrador puede cambiarlas y agregar más.
	 */
	private static final String[][] CATEGORIAS = {
			{ "bailar", "Discotecas y sitios para bailar", "amenity=nightclub", "40000" },
			{ "comer", "Restaurantes, panaderías y comidas rápidas", "amenity=restaurant,amenity=fast_food", "30000" },
			{ "tomar", "Bares, cafés y sitios para tomar algo", "amenity=bar,amenity=pub,amenity=cafe", "25000" },
			{ "jugar", "Videojuegos, bolos, billar y juegos de mesa",
					"leisure=bowling_alley,leisure=amusement_arcade,leisure=escape_game", "25000" } };

	/**
	 * Zonas iniciales, por franjas de calles: nombre, latitud mínima y latitud máxima.
	 */
	private static final String[][] ZONAS = { { "Calle 75 a calle 100", "4.655", "4.686" },
			{ "Calle 100 a calle 127", "4.686", "4.706" }, { "Calle 127 a calle 170", "4.706", "4.754" },
			{ "Calle 170 a calle 222", "4.754", "4.800" } };

	/**
	 * Crea las categorías básicas que todavía no existan. A las que ya existen sin etiquetas de
	 * OpenStreetMap les asigna las etiquetas y el precio de referencia.
	 *
	 * @param categoriaRepo repositorio de categorías
	 * @return tarea que se ejecuta al iniciar la aplicación
	 */
	@Bean
	CommandLineRunner initCategorias(CategoriaRepository categoriaRepo) {
		return args -> {
			for (String[] datos : CATEGORIAS) {
				Categoria c = categoriaRepo.findByNombreIgnoreCase(datos[0]);
				if (c == null) {
					c = new Categoria();
					c.setNombre(datos[0]);
					c.setDescripcion(datos[1]);
					log.info("Precargando categoría {}", datos[0]);
				}
				if (c.getEtiquetaOsm() == null) {
					c.setEtiquetaOsm(datos[2]);
					c.setPrecioReferencia(new BigDecimal(datos[3]));
				}
				categoriaRepo.save(c);
			}
		};
	}

	/**
	 * Crea las zonas iniciales si todavía no hay ninguna.
	 *
	 * @param zonaRepo repositorio de zonas
	 * @return tarea que se ejecuta al iniciar la aplicación
	 */
	@Bean
	CommandLineRunner initZonas(ZonaRepository zonaRepo) {
		return args -> {
			if (zonaRepo.count() > 0) {
				return;
			}
			for (String[] datos : ZONAS) {
				Zona z = new Zona();
				z.setNombre(datos[0]);
				z.setGastoMinimo(BigDecimal.ZERO);
				z.setGastoMaximo(new BigDecimal("200000"));
				z.setLatitudMinima(Double.valueOf(datos[1]));
				z.setLatitudMaxima(Double.valueOf(datos[2]));
				zonaRepo.save(z);
				log.info("Precargando zona {}", datos[0]);
			}
		};
	}
}
