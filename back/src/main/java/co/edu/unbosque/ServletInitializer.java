package co.edu.unbosque;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador que permite desplegar la aplicación como archivo WAR en un servidor de
 * aplicaciones externo, como Tomcat.
 */
public class ServletInitializer extends SpringBootServletInitializer {

	/**
	 * Indica la clase principal que se usa para configurar la aplicación al desplegarla.
	 *
	 * @param application constructor de la aplicación Spring
	 * @return el constructor configurado con {@link BogoparcheApplication}
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BogoparcheApplication.class);
	}

}
