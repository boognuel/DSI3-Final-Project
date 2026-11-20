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

	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = true;
		this.emailVerificado = false;
		this.role = Role.USER;
		this.fechaCreacion = LocalDateTime.now();
	}

	public enum Role {
		USER, ADMIN
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	// Una cuenta sin correo verificado no puede iniciar sesión
	@Override
	public boolean isEnabled() {
		return enabled && emailVerificado;
	}
}
