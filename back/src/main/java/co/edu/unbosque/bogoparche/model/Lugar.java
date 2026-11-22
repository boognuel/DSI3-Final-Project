package co.edu.unbosque.bogoparche.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plan que crea el equipo (por ejemplo una panadería o una discoteca), con su ubicación, su
 * categoría, su zona y lo que gasta en promedio una persona. Los lugares que llegan de OpenStreetMap
 * guardan su identificador allí y, mientras el administrador no corrija el precio, lo tienen estimado.
 */
@Entity
@Table(name = "lugar")
@Getter
@Setter
@NoArgsConstructor
public class Lugar {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nombre;
	@Column(length = 500)
	private String descripcion;
	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;
	@ManyToOne
	@JoinColumn(name = "zona_id")
	private Zona zona;
	private String direccion;
	private Double latitud;
	private Double longitud;
	@Column(precision = 14, scale = 2)
	private BigDecimal costoEstimado;
	private boolean precioEstimado;
	@Column(unique = true, length = 40)
	private String osmId;
}
