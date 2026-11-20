package co.edu.unbosque.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import co.edu.unbosque.model.ModoTransporte;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Entrada: salidaId, lugarId, origenLatitud, origenLongitud, modoTransporte.
// Salida: además distancia, duración y la geometría del trayecto (GeoJSON) para dibujarla en Leaflet.
@Data
public class RutaDTO {
	@NotNull
	private Long salidaId;
	@NotNull
	private Long lugarId;
	@NotNull
	@DecimalMin("-90")
	@DecimalMax("90")
	private Double origenLatitud;
	@NotNull
	@DecimalMin("-180")
	@DecimalMax("180")
	private Double origenLongitud;
	private ModoTransporte modoTransporte = ModoTransporte.CARRO;

	private String destinoNombre;
	private Integer distanciaMetros;
	private Integer duracionSegundos;
	private Integer duracionMinutos;
	@JsonRawValue
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String geometriaGeoJson;
}
