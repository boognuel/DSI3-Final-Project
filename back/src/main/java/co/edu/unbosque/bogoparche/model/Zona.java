package co.edu.unbosque.bogoparche.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Zona de Bogotá, de la calle 75 hacia el norte, con el rango de gasto por persona de sus
 * lugares. Las recomendaciones parten de este rango y del presupuesto del usuario.
 */
@Entity
@Table(name = "zona")
@Getter
@Setter
@NoArgsConstructor
public class Zona {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 80)
	private String nombre;
	private String descripcion;
	@Column(precision = 14, scale = 2)
	private BigDecimal gastoMinimo;
	@Column(precision = 14, scale = 2)
	private BigDecimal gastoMaximo;
	private Double latitudMinima;
	private Double latitudMaxima;
}
