package co.edu.unbosque.bogoparche.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Salida con amigos que organiza un usuario, con su presupuesto, sus invitaciones, los lugares
 * propuestos y las rutas calculadas.
 */
@Entity
@Table(name = "salida")
@Getter
@Setter
@NoArgsConstructor
public class Salida {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String titulo;
	@Column(length = 1000)
	private String descripcion;
	@ManyToOne(optional = false)
	@JoinColumn(name = "organizador_id")
	private User organizador;
	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal presupuesto;
	private String zonaNombre;
	private Double latitudCentro;
	private Double longitudCentro;
	private Integer radioMetros;
	private LocalDateTime fechaHora;
	@Enumerated(EnumType.STRING)
	private EstadoSalida estado = EstadoSalida.PLANEANDO;

	@OneToMany(mappedBy = "salida", cascade = CascadeType.REMOVE)
	private List<Invitacion> invitaciones = new ArrayList<>();
	@OneToMany(mappedBy = "salida", cascade = CascadeType.REMOVE)
	private List<SalidaLugar> lugares = new ArrayList<>();
	@OneToMany(mappedBy = "salida", cascade = CascadeType.REMOVE)
	private List<Ruta> rutas = new ArrayList<>();
}
