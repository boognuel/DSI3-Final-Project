package co.edu.unbosque.bogoparche.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import co.edu.unbosque.bogoparche.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los tokens JWT de la sesión, firmados con HS256 y válidos por 24 horas.
 */
@Component
public class JwtUtil {
	private static final long JWT_TOKEN_VALIDITY = 24L * 60 * 60 * 1000; // 24 horas

	@Value("${jwt.secret}")
	private String secret;

	/**
	 * Construye la llave de firma a partir de jwt.secret.
	 *
	 * @return llave HMAC
	 */
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Obtiene el nombre de usuario del token.
	 *
	 * @param token token JWT
	 * @return nombre de usuario
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Obtiene la fecha de vencimiento del token.
	 *
	 * @param token token JWT
	 * @return fecha de vencimiento
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Obtiene un dato del token.
	 *
	 * @param <T>            tipo del dato
	 * @param token          token JWT
	 * @param claimsResolver función que toma el dato de los claims
	 * @return el dato
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	/**
	 * Verifica la firma del token y devuelve sus datos.
	 *
	 * @param token token JWT
	 * @return claims del token
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}

	/**
	 * Indica si el token ya venció.
	 *
	 * @param token token JWT
	 * @return {@code true} si venció
	 */
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Genera el token de sesión con el nombre de usuario y el rol.
	 *
	 * @param userDetails usuario que inicia sesión
	 * @return token JWT firmado
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		if (userDetails instanceof User user) {
			claims.put("role", user.getRole().name());
		}
		return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	/**
	 * Indica si el token pertenece al usuario y no ha vencido.
	 *
	 * @param token       token JWT
	 * @param userDetails usuario cargado de la base de datos
	 * @return {@code true} si el token es válido
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
	}
}
