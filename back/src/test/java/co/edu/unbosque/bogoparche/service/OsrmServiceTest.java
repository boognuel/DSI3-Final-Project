package co.edu.unbosque.bogoparche.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.ServicioExternoException;
import co.edu.unbosque.bogoparche.model.ModoTransporte;

/**
 * Pruebas de cómo se arma la consulta a OSRM y cómo se lee su respuesta, sin llamar al servidor.
 */
class OsrmServiceTest {
	private OsrmService osrmService;

	/**
	 * Crea el servicio con la URL del servidor de OSRM.
	 */
	@BeforeEach
	void setUp() {
		osrmService = new OsrmService();
		ReflectionTestUtils.setField(osrmService, "url", "https://routing.openstreetmap.de");
	}

	/**
	 * La URL usa el servidor del modo de transporte y pone las coordenadas como longitud,latitud con
	 * punto decimal.
	 */
	@Test
	void urlUsaElModoYElOrdenLongitudLatitud() {
		assertEquals("https://routing.openstreetmap.de/routed-foot/route/v1/driving/-74.056000,4.668000;"
				+ "-74.030500,4.695000?overview=full&geometries=geojson&steps=true",
				osrmService.armarUrl(ModoTransporte.PIE, 4.668, -74.056, 4.695, -74.0305));
	}

	/**
	 * De la respuesta se toman la distancia, la duración, la línea del camino y los pasos.
	 */
	@Test
	void leeDistanciaDuracionLineaYPasos() {
		String json = """
				{"code":"Ok","routes":[{"distance":4120.5,"duration":610.2,
				"geometry":{"type":"LineString","coordinates":[[-74.056,4.668],[-74.0305,4.695]]},
				"legs":[{"steps":[
				{"name":"Calle 85","distance":350.0,"maneuver":{"type":"depart","modifier":"right"}},
				{"name":"","distance":0,"maneuver":{"type":"arrive"}}]}]}]}""";
		OsrmService.Trayecto t = osrmService.interpretar(json);
		assertEquals(4121, t.distanciaMetros());
		assertEquals(610, t.duracionSegundos());
		assertTrue(t.geometriaGeoJson().contains("\"LineString\""));
		assertEquals("depart right Calle 85 (350 m)", t.pasos().get(0));
		assertEquals("arrive (0 m)", t.pasos().get(1));
	}

	/**
	 * Si no hay camino por las calles se responde 400 con un mensaje claro.
	 */
	@Test
	void sinCaminoRespondeDatosInvalidos() {
		assertThrows(DatosInvalidosException.class,
				() -> osrmService.interpretar("{\"code\":\"NoRoute\",\"message\":\"Impossible route\"}"));
	}

	/**
	 * Una respuesta que no es JSON, por ejemplo una página de error, se trata como servicio caído.
	 */
	@Test
	void respuestaNoValidaEsServicioCaido() {
		assertThrows(ServicioExternoException.class, () -> osrmService.interpretar("<html>502 Bad Gateway</html>"));
	}
}
