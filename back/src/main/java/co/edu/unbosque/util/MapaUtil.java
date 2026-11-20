package co.edu.unbosque.util;

import java.util.Locale;

/**
 * Enlaces a Google Maps para ver en el mapa la ubicación guardada en la auditoría.
 */
public final class MapaUtil {

	/**
	 * Clase de utilidades: no se instancia.
	 */
	private MapaUtil() {
	}

	/**
	 * Arma el enlace de Google Maps que muestra un punto.
	 *
	 * @param latitud  latitud, o nula
	 * @param longitud longitud, o nula
	 * @return enlace al punto, o {@code null} si falta alguna coordenada
	 */
	public static String enlaceGoogleMaps(Double latitud, Double longitud) {
		if (latitud == null || longitud == null) {
			return null;
		}
		return String.format(Locale.ROOT, "https://www.google.com/maps?q=%.6f,%.6f", latitud, longitud);
	}
}
