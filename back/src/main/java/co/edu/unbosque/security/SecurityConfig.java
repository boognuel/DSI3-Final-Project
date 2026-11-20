package co.edu.unbosque.security;

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
import co.edu.unbosque.service.AuditoriaService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/invitacion/responder").permitAll()
						.requestMatchers("/", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/admin/**", "/user/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

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

	// Spring Security arma solo el proveedor de autenticación con UserDetailsServiceImpl y este PasswordEncoder
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
