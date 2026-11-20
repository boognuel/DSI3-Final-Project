package co.edu.unbosque.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Cuenta de usuario. Implementa UserDetails para que Spring Security la use al iniciar sesión.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class User implements UserDetails {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true, nullable = false)
	private String username;
	@Column(unique = true, nullable = false)
	private String email;
	private String password;
	@Enumerated(EnumType.STRING)
	private Role role;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private boolean emailVerificado;
	private LocalDateTime fechaCreacion;

	/**
	 * Crea una cuenta activa con rol USER, sin el correo verificado y con la fecha actual.
	 */
	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = true;
		this.emailVerificado = false;
		this.role = Role.USER;
		this.fechaCreacion = LocalDateTime.now();
	}

	/**
	 * Roles de la aplicación: cliente o administrador.
	 */
	public enum Role {
		USER, ADMIN
	}

	/**
	 * Permisos del usuario según su rol, con el prefijo ROLE_ que usa Spring Security.
	 *
	 * @return autoridad del rol
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	/**
	 * Indica si la cuenta puede iniciar sesión: debe estar activa y con el correo verificado.
	 *
	 * @return {@code true} si puede iniciar sesión
	 */
	@Override
	public boolean isEnabled() {
		return enabled && emailVerificado;
	}
}
