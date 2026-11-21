package co.edu.unbosque.bogoparche.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import co.edu.unbosque.bogoparche.model.ModoTransporte;

/**
 * Pruebas de cómo se arma la petición a Groq y cómo se lee su respuesta, sin llamar al servicio.
 */
class GroqServiceTest {
	private GroqService groqService;
	private OsrmService.Trayecto trayecto;

	/**
	 * Crea el servicio con su configuración y un trayecto de prueba.
	 */
	@BeforeEach
	void setUp() {
		groqService = new GroqService();
		ReflectionTestUtils.setField(groqService, "url", "https://api.groq.com/openai/v1/chat/completions");
		ReflectionTestUtils.setField(groqService, "modelo", "openai/gpt-oss-120b");
		ReflectionTestUtils.setField(groqService, "apiKey", "");
		trayecto = new OsrmService.Trayecto(4120, 610, "{}", List.of("depart right Calle 85 (350 m)"));
	}

	/**
	 * La petición lleva el modelo configurado y los datos reales del recorrido.
	 */
	@Test
	void peticionLlevaModeloYDatosDelRecorrido() {
		String cuerpo = groqService.armarCuerpo("Panadería La Esquina, Calle 119", ModoTransporte.PIE, trayecto);
		assertTrue(cuerpo.contains("\"model\":\"openai/gpt-oss-120b\""));
		assertTrue(cuerpo.contains("Destino: Panadería La Esquina, Calle 119. Modo: pie. Distancia: 4.1 km. "
				+ "Tiempo: 10 min. Pasos: depart right Calle 85 (350 m)"));
	}

	/**
	 * El texto de las indicaciones se toma de la primera respuesta.
	 */
	@Test
	void leeElTextoDeLaPrimeraRespuesta() {
		assertEquals("1. Sal por la Calle 85.", groqService.leerTexto(
				"{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\" 1. Sal por la Calle 85. \"}}]}"));
		assertNull(groqService.leerTexto("{\"choices\":[]}"));
	}

	/**
	 * Sin clave configurada no se llama a Groq y la ruta queda sin indicaciones.
	 */
	@Test
	void sinClaveNoHayIndicaciones() {
		assertNull(groqService.redactarIndicaciones("Panadería", ModoTransporte.CARRO, trayecto));
	}
}
