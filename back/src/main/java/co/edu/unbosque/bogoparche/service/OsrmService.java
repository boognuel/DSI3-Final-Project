package co.edu.unbosque.bogoparche.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.ServicioExternoException;
import co.edu.unbosque.bogoparche.model.ModoTransporte;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Calcula el camino por las calles con OSRM, el motor de rutas que usa el mapa de OpenStreetMap.
 * Solo se envían las coordenadas de origen y destino.
 */
@Service
public class OsrmService {
	private static final Logger log = LoggerFactory.getLogger(OsrmService.class);
	private static final JsonMapper JSON = JsonMapper.builder().build();
	private static final String NO_DISPONIBLE = "El servicio de rutas no está disponible, intenta más tarde";
	// Límite de pasos que se le pasan a Groq, para no enviar textos enormes en recorridos largos
	private static final int MAX_PASOS = 40;

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	@Value("${osrm.url}")
	private String url;

	/**
	 * Resultado de OSRM: distancia, duración, la línea del camino y los pasos del recorrido.
	 *
	 * @param distanciaMetros  distancia total en metros
	 * @param duracionSegundos duración aproximada en segundos, sin tráfico
	 * @param geometriaGeoJson línea del camino en GeoJSON, para dibujarla en el mapa
	 * @param pasos            giros del recorrido, por ejemplo "turn left Carrera 15 (1200 m)"
	 */
	public record Trayecto(int distanciaMetros, int duracionSegundos, String geometriaGeoJson, List<String> pasos) {
	}

	/**
	 * Pide a OSRM el camino entre el origen y el destino.
	 *
	 * @param modo       modo de transporte
	 * @param origenLat  latitud de origen
	 * @param origenLon  longitud de origen
	 * @param destinoLat latitud de destino
	 * @param destinoLon longitud de destino
	 * @return trayecto calculado
	 * @throws DatosInvalidosException  si no hay camino por las calles entre los dos puntos
	 * @throws ServicioExternoException si OSRM no responde o responde con un error
	 */
	public Trayecto calcular(ModoTransporte modo, double origenLat, double origenLon, double destinoLat,
			double destinoLon) {
		HttpRequest peticion = HttpRequest
				.newBuilder(URI.create(armarUrl(modo, origenLat, origenLon, destinoLat, destinoLon)))
				.timeout(Duration.ofSeconds(15)).GET().build();
		try {
			return interpretar(http.send(peticion, HttpResponse.BodyHandlers.ofString()).body());
		} catch (IOException e) {
			log.error("No se pudo consultar OSRM: {}", e.getMessage());
			throw new ServicioExternoException(NO_DISPONIBLE);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
	}

	/**
	 * Arma la URL de OSRM. El servidor cambia según el modo de transporte y las coordenadas van en
	 * orden longitud,latitud con punto decimal.
	 *
	 * @param modo       modo de transporte
	 * @param origenLat  latitud de origen
	 * @param origenLon  longitud de origen
	 * @param destinoLat latitud de destino
	 * @param destinoLon longitud de destino
	 * @return URL de la consulta
	 */
	String armarUrl(ModoTransporte modo, double origenLat, double origenLon, double destinoLat, double destinoLon) {
		String perfil = switch (modo) {
		case PIE -> "routed-foot";
		case BICICLETA -> "routed-bike";
		case CARRO -> "routed-car";
		};
		return String.format(Locale.ROOT,
				"%s/%s/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson&steps=true", url, perfil,
				origenLon, origenLat, destinoLon, destinoLat);
	}

	/**
	 * Lee la respuesta de OSRM.
	 *
	 * @param cuerpo JSON de la respuesta
	 * @return trayecto de la primera ruta
	 * @throws DatosInvalidosException  si OSRM no encontró camino
	 * @throws ServicioExternoException si la respuesta no es válida
	 */
	Trayecto interpretar(String cuerpo) {
		RespuestaOsrm r;
		try {
			r = JSON.readValue(cuerpo, RespuestaOsrm.class);
		} catch (JacksonException e) {
			log.error("Respuesta de OSRM no válida: {}", e.getOriginalMessage());
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
		if ("NoRoute".equals(r.code())) {
			throw new DatosInvalidosException("No hay un camino por las calles entre tu ubicación y el lugar");
		}
		if (!"Ok".equals(r.code()) || r.routes() == null || r.routes().isEmpty()) {
			log.error("OSRM respondió con el código {}", r.code());
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
		RutaOsrm ruta = r.routes().get(0);
		List<String> pasos = new ArrayList<>();
		if (ruta.legs() != null) {
			for (TramoOsrm tramo : ruta.legs()) {
				if (tramo.steps() != null) {
					tramo.steps().stream().map(OsrmService::describir).forEach(pasos::add);
				}
			}
		}
		return new Trayecto((int) Math.round(ruta.distance()), (int) Math.round(ruta.duration()),
				ruta.geometry() == null ? null : ruta.geometry().toString(),
				List.copyOf(pasos.subList(0, Math.min(pasos.size(), MAX_PASOS))));
	}

	/**
	 * Describe un paso con los datos de OSRM, por ejemplo "turn left Carrera 15 (1200 m)".
	 *
	 * @param p paso de OSRM
	 * @return descripción del paso
	 */
	private static String describir(PasoOsrm p) {
		StringBuilder sb = new StringBuilder();
		if (p.maneuver() != null) {
			sb.append(p.maneuver().type());
			if (p.maneuver().modifier() != null) {
				sb.append(" ").append(p.maneuver().modifier());
			}
		}
		if (p.name() != null && !p.name().isBlank()) {
			sb.append(" ").append(p.name());
		}
		return sb.append(" (").append(Math.round(p.distance())).append(" m)").toString().trim();
	}

	/**
	 * Respuesta de OSRM.
	 *
	 * @param code   "Ok" si encontró ruta
	 * @param routes rutas encontradas
	 */
	private record RespuestaOsrm(String code, List<RutaOsrm> routes) {
	}

	/**
	 * Ruta de OSRM.
	 *
	 * @param distance distancia en metros
	 * @param duration duración en segundos
	 * @param geometry línea en GeoJSON
	 * @param legs     tramos entre los puntos
	 */
	private record RutaOsrm(double distance, double duration, JsonNode geometry, List<TramoOsrm> legs) {
	}

	/**
	 * Tramo de una ruta de OSRM.
	 *
	 * @param steps pasos del tramo
	 */
	private record TramoOsrm(List<PasoOsrm> steps) {
	}

	/**
	 * Paso de un tramo de OSRM.
	 *
	 * @param name     nombre de la vía
	 * @param distance distancia del paso en metros
	 * @param maneuver maniobra al iniciar el paso
	 */
	private record PasoOsrm(String name, double distance, Maniobra maneuver) {
	}

	/**
	 * Maniobra de un paso de OSRM.
	 *
	 * @param type     tipo, por ejemplo "turn" o "depart"
	 * @param modifier dirección, por ejemplo "left"
	 */
	private record Maniobra(String type, String modifier) {
	}
}
