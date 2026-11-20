package co.edu.unbosque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación BogoParche. Arranca el contexto de Spring Boot y escanea los
 * paquetes de configuración, controladores, servicios, repositorios y seguridad que están debajo
 * de {@code co.edu.unbosque}.
 */
@SpringBootApplication
public class BogoparcheApplication {

	/**
	 * Punto de entrada de la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(BogoparcheApplication.class, args);
	}

}
