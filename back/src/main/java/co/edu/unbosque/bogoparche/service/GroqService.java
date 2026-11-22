package co.edu.unbosque.bogoparche.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.GroqMensajeDTO;
import co.edu.unbosque.bogoparche.dto.GroqRespuestaDTO;
import co.edu.unbosque.bogoparche.dto.GroqSolicitudDTO;
import co.edu.unbosque.bogoparche.dto.TrayectoDTO;
import co.edu.unbosque.bogoparche.exception.ServicioExternoException;
import co.edu.unbosque.bogoparche.model.ModoTransporte;

/**
 * Redacta en español las indicaciones para llegar a un lugar a partir de los pasos que calculó
 * OSRM, usando un modelo de lenguaje de Groq. Groq no calcula el camino: solo lo explica.
 */
@Service
public class GroqService {
	static final String NO_DISPONIBLE = "Las indicaciones no están disponibles en este momento, intenta más tarde";
	private static final String INSTRUCCIONES = "Eres un asistente de BogoParche. Redacta indicaciones breves y "
			+ "claras en español, en una lista numerada, para llegar a un plan en Bogotá. Usa solo los datos que se "
			+ "te entregan y no inventes calles ni lugares.";

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
	 * @return texto de las indicaciones
	 * @throws ServicioExternoException si Groq no está configurado, no responde o responde vacío
	 */
	public String redactarIndicaciones(String destino, ModoTransporte modo, TrayectoDTO trayecto) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
		String texto = leerTexto(
				ExternalHTTPRequestHandler.doPostGroqAndConvertToDto(apiKey, armarSolicitud(destino, modo, trayecto)));
		if (texto == null) {
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
		return texto;
	}

	/**
	 * Arma la petición con los datos del recorrido.
	 *
	 * @param destino  nombre y dirección del lugar
	 * @param modo     modo de transporte
	 * @param trayecto camino calculado por OSRM
	 * @return petición para Groq
	 */
	GroqSolicitudDTO armarSolicitud(String destino, ModoTransporte modo, TrayectoDTO trayecto) {
		String datos = String.format(Locale.ROOT, "Destino: %s. Modo: %s. Distancia: %.1f km. Tiempo: %d min. Pasos: %s",
				destino, modo.name().toLowerCase(Locale.ROOT), trayecto.getDistanciaMetros() / 1000.0,
				Math.round(trayecto.getDuracionSegundos() / 60.0), String.join("; ", trayecto.getPasos()));
		List<GroqMensajeDTO> mensajes = new ArrayList<>();
		mensajes.add(mensaje("system", INSTRUCCIONES));
		mensajes.add(mensaje("user", datos));
		GroqSolicitudDTO s = new GroqSolicitudDTO();
		s.setModel(modelo);
		s.setTemperature(0.3);
		s.setMessages(mensajes);
		return s;
	}

	/**
	 * Lee el texto de la respuesta de Groq.
	 *
	 * @param r respuesta convertida, o null si Groq no respondió
	 * @return texto de la primera respuesta, o null si no hay
	 */
	String leerTexto(GroqRespuestaDTO r) {
		if (r == null || r.getChoices() == null || r.getChoices().isEmpty()
				|| r.getChoices().get(0).getMessage() == null) {
			return null;
		}
		String texto = r.getChoices().get(0).getMessage().getContent();
		return texto == null || texto.isBlank() ? null : texto.trim();
	}

	/**
	 * Crea un mensaje de la conversación.
	 *
	 * @param rol   system o user
	 * @param texto contenido
	 * @return mensaje
	 */
	private GroqMensajeDTO mensaje(String rol, String texto) {
		GroqMensajeDTO m = new GroqMensajeDTO();
		m.setRole(rol);
		m.setContent(texto);
		return m;
	}
}
