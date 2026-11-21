package co.edu.unbosque.bogoparche.util;

import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;

/**
 * Valida que una ubicación esté en el área que cubre la aplicación: Bogotá, de la calle 75 hacia
 * el norte. Los límites son aproximados y forman un rectángulo.
 */
public class UbicacionValidator {

	/** Latitud aproximada de la calle 75. */
	public static final double LATITUD_MINIMA = 4.655;
	/** Latitud del límite norte de la ciudad. */
	public static final double LATITUD_MAXIMA = 4.840;
	/** Longitud del borde occidental (Suba). */
	public static final double LONGITUD_MINIMA = -74.130;
	/** Longitud del borde oriental (cerros). */
	public static final double LONGITUD_MAXIMA = -73.990;

	/**
	 * Clase de utilidad: no se instancia.
	 */
	private UbicacionValidator() {
	}

	/**
	 * Valida que las coordenadas estén dentro del área de la aplicación.
	 *
	 * @param latitud  latitud en grados
	 * @param longitud longitud en grados
	 * @throws DatosInvalidosException si falta alguna coordenada o está fuera del área
	 */
	public static void validar(Double latitud, Double longitud) {
		if (latitud == null || longitud == null) {
			throw new DatosInvalidosException("La latitud y la longitud son obligatorias");
		}
		if (latitud < LATITUD_MINIMA || latitud > LATITUD_MAXIMA || longitud < LONGITUD_MINIMA
				|| longitud > LONGITUD_MAXIMA) {
			throw new DatosInvalidosException("El lugar debe estar en Bogotá, de la calle 75 hacia el norte");
		}
	}
}
