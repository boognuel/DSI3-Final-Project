package co.edu.unbosque.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración general de la aplicación. Registra los beans compartidos por los servicios.
 */
@Configuration
public class AppConfig {

	/**
	 * Crea el {@link ModelMapper} que convierte entidades en DTO y viceversa.
	 *
	 * @return instancia de ModelMapper
	 */
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
