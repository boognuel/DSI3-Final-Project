package co.edu.unbosque.bogoparche.security;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que lee el token JWT de la cabecera Authorization y, si es válido, deja al usuario
 * autenticado para esa petición.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	/**
	 * Crea el filtro.
	 *
	 * @param jwtUtil            utilidades para leer y validar el token
	 * @param userDetailsService servicio que carga el usuario
	 */
	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Autentica la petición si trae un token válido de una cuenta habilitada. Si el token no es
	 * válido, la petición sigue sin autenticar y Spring Security decide si la rechaza.
	 *
	 * @param request     petición HTTP
	 * @param response    respuesta HTTP
	 * @param filterChain cadena de filtros
	 * @throws ServletException si falla un filtro siguiente
	 * @throws IOException      si falla la lectura o escritura
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			String jwt = header.substring(7);
			try {
				String username = jwtUtil.extractUsername(jwt);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				if (userDetails.isEnabled() && jwtUtil.validateToken(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (Exception e) {
				logger.warn("Token JWT inválido o usuario inexistente");
			}
		}
		filterChain.doFilter(request, response);
	}
}
