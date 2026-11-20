package co.edu.unbosque.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import co.edu.unbosque.model.EstadoSalida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datos de una salida con amigos: título, presupuesto, organizador, fecha y estado.
 */
@Data
public class SalidaDTO {
	private Long id;
	@NotBlank
	private String titulo;
	private String descripcion;
	private Long organizadorId;
	private String organizadorNombre;
	@NotNull
	@DecimalMin("0.0")
	private BigDecimal presupuesto;
	private String zonaNombre;
	private Double latitudCentro;
	private Double longitudCentro;
	private Integer radioMetros;
	private LocalDateTime fechaHora;
	private EstadoSalida estado;
}
