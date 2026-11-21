package co.edu.unbosque.bogoparche.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import co.edu.unbosque.bogoparche.service.AuditoriaService;

/**
 * Configuración de seguridad: autenticación sin estado con JWT, permisos por ruta y rol, y CORS
 * para el frontend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthFilter;

	/**
	 * Crea la configuración con el filtro que valida el token JWT de cada petición.
	 *
	 * @param jwtAuthFilter filtro JWT
	 */
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	/**
	 * Define qué rutas son públicas, cuáles requieren el rol ADMIN y cuáles solo una sesión válida.
	 * Las peticiones sin token válido a rutas protegidas reciben 401.
	 *
	 * @param http configuración HTTP de Spring Security
	 * @return cadena de filtros de seguridad
	 * @throws Exception si la configuración no se puede construir
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/invitacion/responder").permitAll()
						.requestMatchers("/", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						// El catálogo lo consulta cualquier usuario con sesión, pero solo el admin lo modifica
						.requestMatchers(HttpMethod.GET, "/categoria/**", "/zona/**", "/lugar/**").authenticated()
						.requestMatchers("/admin/**", "/user/**", "/categoria/**", "/zona/**", "/lugar/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/**
	 * Permite las peticiones del frontend, incluidas las cabeceras de ubicación que usa la auditoría,
	 * y expone Content-Disposition para la descarga del reporte.
	 *
	 * @param origin origen permitido (app.cors.allowed-origin)
	 * @return configuración CORS
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}") String origin) {
		CorsConfiguration c = new CorsConfiguration();
		c.setAllowedOrigins(List.of(origin));
		c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		c.setAllowedHeaders(List.of("Authorization", "Content-Type", AuditoriaService.CABECERA_LATITUD,
				AuditoriaService.CABECERA_LONGITUD));
		c.setExposedHeaders(List.of("Content-Disposition"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", c);
		return source;
	}

	/**
	 * Codificador de contraseñas con BCrypt. Spring Security arma el proveedor de autenticación con
	 * UserDetailsServiceImpl y este codificador.
	 *
	 * @return codificador BCrypt
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Expone el AuthenticationManager que usa el inicio de sesión.
	 *
	 * @param config configuración de autenticación de Spring
	 * @return gestor de autenticación
	 * @throws Exception si no se puede obtener
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
