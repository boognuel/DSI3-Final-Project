package co.edu.unbosque.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "salida_lugar", uniqueConstraints = @UniqueConstraint(columnNames = { "salida_id", "lugar_id" }))
@Getter
@Setter
@NoArgsConstructor
public class SalidaLugar {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "salida_id")
	private Salida salida;
	@ManyToOne(optional = false)
	@JoinColumn(name = "lugar_id")
	private Lugar lugar;
	private int orden;
	private boolean seleccionado;
	@Column(precision = 14, scale = 2)
	private BigDecimal costoEstimadoPorPersona;
}
