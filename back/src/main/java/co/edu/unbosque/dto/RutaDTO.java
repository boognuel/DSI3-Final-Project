package co.edu.unbosque.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import co.edu.unbosque.model.ModoTransporte;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Ruta hacia un lugar de una salida. En la petición se envían la salida, el lugar, la ubicación del
 * usuario y el modo de transporte; en la respuesta llegan además la distancia, la duración, la línea
 * del camino en GeoJSON para dibujarla en el mapa y las indicaciones en español.
 */
@Data
public class RutaDTO {
	@NotNull(message = "La salida es obligatoria")
	private Long salidaId;
	@NotNull(message = "El lugar es obligatorio")
	private Long lugarId;
	@NotNull(message = "La latitud de origen es obligatoria")
	@DecimalMin(value = "-90", message = "La latitud de origen debe estar entre -90 y 90")
	@DecimalMax(value = "90", message = "La latitud de origen debe estar entre -90 y 90")
	private Double origenLatitud;
	@NotNull(message = "La longitud de origen es obligatoria")
	@DecimalMin(value = "-180", message = "La longitud de origen debe estar entre -180 y 180")
	@DecimalMax(value = "180", message = "La longitud de origen debe estar entre -180 y 180")
	private Double origenLongitud;
	private ModoTransporte modoTransporte = ModoTransporte.CARRO;

	private String destinoNombre;
	private Integer distanciaMetros;
	private Integer duracionSegundos;
	private Integer duracionMinutos;
	@JsonRawValue
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String geometriaGeoJson;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String indicaciones;
}
