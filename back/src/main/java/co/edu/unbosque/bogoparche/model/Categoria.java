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
 * Tipo de plan que le gusta al usuario, por ejemplo bailar, comer, tomar o jugar. Sirve para
 * clasificar los lugares y filtrar las recomendaciones.
 */
@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 50)
	private String nombre;
	private String descripcion;
	@Column(length = 200)
	private String etiquetaOsm;
	@Column(precision = 14, scale = 2)
	private BigDecimal precioReferencia;
}
