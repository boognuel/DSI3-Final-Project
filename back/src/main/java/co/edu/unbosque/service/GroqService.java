package co.edu.unbosque.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import co.edu.unbosque.model.ModoTransporte;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Redacta en español las indicaciones para llegar a un lugar a partir de los pasos que calculó
 * OSRM, usando un modelo de lenguaje de Groq. Groq no calcula el camino: solo lo explica.
 */
@Service
public class GroqService {
	private static final Logger log = LoggerFactory.getLogger(GroqService.class);
	private static final JsonMapper JSON = JsonMapper.builder().build();
	private static final String INSTRUCCIONES = "Eres un asistente de BogoParche. Redacta indicaciones breves y "
			+ "claras en español, en una lista numerada, para llegar a un plan en Bogotá. Usa solo los datos que se "
			+ "te entregan y no inventes calles ni lugares.";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	@Value("${groq.url}")
	private String url;
	@Value("${groq.model}")
	private String modelo;
	@Value("${groq.api.key:}")
	private String apiKey;

	/**
	 * Pide a Groq las indicaciones del recorrido.
	 *
	 * @param destino  nombre y dirección del lugar
	 * @param modo     modo de transporte
	 * @param trayecto camino calculado por OSRM
	 * @return texto de las indicaciones, o {@code null} si Groq no está configurado o no respondió
	 */
	public String redactarIndicaciones(String destino, ModoTransporte modo, OsrmService.Trayecto trayecto) {
		if (apiKey == null || apiKey.isBlank()) {
			log.warn("GROQ_API_KEY no está configurada; la ruta se entrega sin indicaciones");
			return null;
		}
		try {
			HttpRequest peticion = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30))
					.header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(armarCuerpo(destino, modo, trayecto))).build();
			HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
			if (respuesta.statusCode() != 200) {
				log.warn("Groq respondió con el estado {}", respuesta.statusCode());
				return null;
			}
			return leerTexto(respuesta.body());
		} catch (IOException | JacksonException e) {
			log.warn("No se pudo consultar Groq: {}", e.getMessage());
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	/**
	 * Arma el cuerpo JSON de la petición con los datos del recorrido.
	 *
	 * @param destino  nombre y dirección del lugar
	 * @param modo     modo de transporte
	 * @param trayecto camino calculado por OSRM
	 * @return cuerpo de la petición
	 */
	String armarCuerpo(String destino, ModoTransporte modo, OsrmService.Trayecto trayecto) {
		String datos = String.format(Locale.ROOT, "Destino: %s. Modo: %s. Distancia: %.1f km. Tiempo: %d min. Pasos: %s",
				destino, modo.name().toLowerCase(Locale.ROOT), trayecto.distanciaMetros() / 1000.0,
				Math.round(trayecto.duracionSegundos() / 60.0), String.join("; ", trayecto.pasos()));
		return JSON.writeValueAsString(Map.of("model", modelo, "temperature", 0.3, "messages",
				List.of(Map.of("role", "system", "content", INSTRUCCIONES), Map.of("role", "user", "content", datos))));
	}

	/**
	 * Lee el texto de la respuesta de Groq.
	 *
	 * @param cuerpo JSON de la respuesta
	 * @return texto de la primera respuesta, o {@code null} si viene vacía
	 */
	String leerTexto(String cuerpo) {
		RespuestaGroq r = JSON.readValue(cuerpo, RespuestaGroq.class);
		if (r.choices() == null || r.choices().isEmpty() || r.choices().get(0).message() == null) {
			return null;
		}
		String texto = r.choices().get(0).message().content();
		return texto == null || texto.isBlank() ? null : texto.trim();
	}

	/**
	 * Respuesta de Groq.
	 *
	 * @param choices respuestas generadas
	 */
	private record RespuestaGroq(List<Opcion> choices) {
	}

	/**
	 * Una respuesta generada.
	 *
	 * @param message mensaje del asistente
	 */
	private record Opcion(Mensaje message) {
	}

	/**
	 * Mensaje del asistente.
	 *
	 * @param content texto
	 */
	private record Mensaje(String content) {
	}
}
