package co.edu.unboque.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lugar", uniqueConstraints = @UniqueConstraint(columnNames = { "osmTipo", "osmId" }))
@Getter
@Setter
@NoArgsConstructor
public class Lugar {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long osmId;
	private String osmTipo;
	private String nombre;
	private String categoria;
	private String direccion;
	private Double latitud;
	private Double longitud;
	@Column(precision = 14, scale = 2)
	private BigDecimal costoEstimado;
}
