package co.edu.unbosque.bogoparche.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ruta calculada desde la ubicación de un usuario hasta un lugar de una salida.
 */
@Entity
@Table(name = "ruta")
@Getter
@Setter
@NoArgsConstructor
public class Ruta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "salida_id")
	private Salida salida;
	@ManyToOne(optional = false)
	@JoinColumn(name = "usuario_id")
	private User usuario;
	@ManyToOne(optional = false)
	@JoinColumn(name = "lugar_destino_id")
	private Lugar lugarDestino;
	private Double origenLatitud;
	private Double origenLongitud;
	private Integer distanciaMetros;
	private Integer duracionSegundos;
	@Enumerated(EnumType.STRING)
	private ModoTransporte modoTransporte;
	private LocalDateTime fechaCalculo;
}
