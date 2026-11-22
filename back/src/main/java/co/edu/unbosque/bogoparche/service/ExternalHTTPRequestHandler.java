package co.edu.unbosque.bogoparche.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import co.edu.unbosque.bogoparche.dto.GroqRespuestaDTO;
import co.edu.unbosque.bogoparche.dto.GroqSolicitudDTO;
import co.edu.unbosque.bogoparche.dto.OsrmRespuestaDTO;
import co.edu.unbosque.bogoparche.dto.OverpassRespuestaDTO;

/**
 * Consume las APIs externas con java.net.http y convierte sus respuestas JSON en DTO con Gson. Cada
 * método devuelve null si la API no responde o responde algo que no se puede leer, y deja el detalle
 * en el log.
 */
public class ExternalHTTPRequestHandler {

	/** API de Overpass para consultar lugares de OpenStreetMap. */
	public static final String URL_OVERPASS = "https://overpass-api.de/api/interpreter";

	/** Servidor de OSRM; se completa con el perfil de transporte y las coordenadas. */
	public static final String URL_OSRM = "https://routing.openstreetmap.de";

	/** API de chat de Groq. */
	public static final String URL_GROQ = "https://api.groq.com/openai/v1/chat/completions";

	/** Agente con el que se identifican las peticiones, como piden las políticas de OpenStreetMap. */
	private static final String USER_AGENT = "Bogoparche/0.1 (proyecto academico)";

	/** Cliente HTTP compartido, con HTTP/2 y un tiempo de espera de conexión de 10 segundos. */
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(10)).build();

	/** Conversor entre JSON y objetos. */
	private static final Gson GSON = new GsonBuilder().create();

	private static final Logger LOG = LoggerFactory.getLogger(ExternalHTTPRequestHandler.class);

	/**
	 * Constructor privado: la clase solo tiene métodos estáticos.
	 */
	private ExternalHTTPRequestHandler() {
	}

	/**
	 * Envía una consulta a Overpass y convierte la respuesta.
	 *
	 * @param consulta consulta en el lenguaje de Overpass
	 * @return elementos encontrados, o null si la API falla
	 */
	public static OverpassRespuestaDTO doPostOverpassAndConvertToDto(String consulta) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_OVERPASS))
				.POST(HttpRequest.BodyPublishers.ofString("data=" + URLEncoder.encode(consulta, StandardCharsets.UTF_8)))
				.timeout(Duration.ofSeconds(90)).header("Content-Type", "application/x-www-form-urlencoded")
				.header("User-Agent", USER_AGENT).build();
		String cuerpo = enviar(request, "Overpass");
		if (cuerpo == null) {
			return null;
		}
		return leer(cuerpo, OverpassRespuestaDTO.class, "Overpass");
	}

	/**
	 * Pide a OSRM una ruta y convierte la respuesta.
	 *
	 * @param url URL de la consulta, con el perfil y las coordenadas
	 * @return respuesta de OSRM, o null si la API falla
	 */
	public static OsrmRespuestaDTO doGetOsrmAndConvertToDto(String url) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(Duration.ofSeconds(15))
				.header("Content-Type", "application/json").header("User-Agent", USER_AGENT).build();
		String cuerpo = enviarConCodigo(request, "OSRM");
		if (cuerpo == null) {
			return null;
		}
		return leer(cuerpo, OsrmRespuestaDTO.class, "OSRM");
	}

	/**
	 * Envía a Groq la petición de las indicaciones y convierte la respuesta.
	 *
	 * @param apiKey    llave de la API de Groq
	 * @param solicitud modelo, temperatura y mensajes
	 * @return respuesta de Groq, o null si la API falla
	 */
	public static GroqRespuestaDTO doPostGroqAndConvertToDto(String apiKey, GroqSolicitudDTO solicitud) {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_GROQ))
				.POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(solicitud))).timeout(Duration.ofSeconds(30))
				.header("Content-Type", "application/json").header("Authorization", "Bearer " + apiKey).build();
		String cuerpo = enviar(request, "Groq");
		if (cuerpo == null) {
			return null;
		}
		return leer(cuerpo, GroqRespuestaDTO.class, "Groq");
	}

	/**
	 * Envía una petición a OSRM. A diferencia de las demás APIs, OSRM responde 400 con un JSON cuando
	 * no hay camino, así que ese cuerpo también se devuelve.
	 *
	 * @param request  petición armada
	 * @param servicio nombre del servicio, para el log
	 * @return cuerpo de la respuesta, o null si falla la conexión o el código no es 2xx ni 400
	 */
	static String enviarConCodigo(HttpRequest request, String servicio) {
		HttpResponse<String> response;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			LOG.error("No fue posible conectar con {}: {}", servicio, e.getMessage());
			return null;
		} catch (InterruptedException e) {
			LOG.error("Se interrumpió la comunicación con {}", servicio);
			Thread.currentThread().interrupt();
			return null;
		}
		if (response.statusCode() == 400 || (response.statusCode() >= 200 && response.statusCode() < 300)) {
			return response.body();
		}
		LOG.error("{} respondió con el código {}", servicio, response.statusCode());
		return null;
	}

	/**
	 * Envía la petición y revisa el código de estado.
	 *
	 * @param request  petición armada
	 * @param servicio nombre del servicio, para el log
	 * @return cuerpo de la respuesta, o null si falla la conexión o el código no es 2xx
	 */
	static String enviar(HttpRequest request, String servicio) {
		HttpResponse<String> response;
		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			LOG.error("No fue posible conectar con {}: {}", servicio, e.getMessage());
			return null;
		} catch (InterruptedException e) {
			LOG.error("Se interrumpió la comunicación con {}", servicio);
			Thread.currentThread().interrupt();
			return null;
		}
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			LOG.error("{} respondió con el código {}", servicio, response.statusCode());
			return null;
		}
		return response.body();
	}

	/**
	 * Convierte el JSON de una respuesta en un DTO.
	 *
	 * @param <T>      tipo del DTO
	 * @param json     cuerpo de la respuesta
	 * @param tipo     clase del DTO
	 * @param servicio nombre del servicio, para el log
	 * @return el DTO, o null si el JSON no es válido
	 */
	static <T> T leer(String json, Class<T> tipo, String servicio) {
		try {
			return GSON.fromJson(json, tipo);
		} catch (JsonSyntaxException e) {
			LOG.error("{} respondió un JSON inválido: {}", servicio, e.getMessage());
			return null;
		}
	}
}
