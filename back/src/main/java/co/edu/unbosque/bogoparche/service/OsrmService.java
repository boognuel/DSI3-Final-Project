package co.edu.unbosque.bogoparche.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.OsrmPasoDTO;
import co.edu.unbosque.bogoparche.dto.OsrmRespuestaDTO;
import co.edu.unbosque.bogoparche.dto.OsrmRutaDTO;
import co.edu.unbosque.bogoparche.dto.OsrmTramoDTO;
import co.edu.unbosque.bogoparche.dto.TrayectoDTO;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.ServicioExternoException;
import co.edu.unbosque.bogoparche.model.ModoTransporte;

/**
 * Calcula el camino por las calles con OSRM, el motor de rutas que usa el mapa de OpenStreetMap.
 * Solo se envían las coordenadas de origen y destino.
 */
@Service
public class OsrmService {
	private static final Logger log = LoggerFactory.getLogger(OsrmService.class);
	private static final String NO_DISPONIBLE = "El servicio de rutas no está disponible, intenta más tarde";
	private static final int MAX_PASOS = 40;

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
	public TrayectoDTO calcular(ModoTransporte modo, double origenLat, double origenLon, double destinoLat,
			double destinoLon) {
		return interpretar(ExternalHTTPRequestHandler.doGetOsrmAndConvertToDto(armarUrl(modo, origenLat, origenLon,
				destinoLat, destinoLon)));
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
		String perfil;
		if (modo == ModoTransporte.PIE) {
			perfil = "routed-foot";
		} else if (modo == ModoTransporte.BICICLETA) {
			perfil = "routed-bike";
		} else {
			perfil = "routed-car";
		}
		return String.format(Locale.ROOT,
				"%s/%s/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson&steps=true",
				ExternalHTTPRequestHandler.URL_OSRM, perfil, origenLon, origenLat, destinoLon, destinoLat);
	}

	/**
	 * Lee la respuesta de OSRM.
	 *
	 * @param r respuesta convertida, o null si OSRM no respondió
	 * @return trayecto de la primera ruta
	 * @throws DatosInvalidosException  si OSRM no encontró camino
	 * @throws ServicioExternoException si la respuesta no es válida
	 */
	TrayectoDTO interpretar(OsrmRespuestaDTO r) {
		if (r == null) {
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
		if ("NoRoute".equals(r.getCode())) {
			throw new DatosInvalidosException("No hay un camino por las calles entre tu ubicación y el lugar");
		}
		if (!"Ok".equals(r.getCode()) || r.getRoutes() == null || r.getRoutes().isEmpty()) {
			log.error("OSRM respondió con el código {}", r.getCode());
			throw new ServicioExternoException(NO_DISPONIBLE);
		}
		OsrmRutaDTO ruta = r.getRoutes().get(0);
		List<String> pasos = new ArrayList<>();
		if (ruta.getLegs() != null) {
			for (OsrmTramoDTO tramo : ruta.getLegs()) {
				if (tramo.getSteps() != null) {
					for (OsrmPasoDTO paso : tramo.getSteps()) {
						if (pasos.size() < MAX_PASOS) {
							pasos.add(describir(paso));
						}
					}
				}
			}
		}
		String geometria = ruta.getGeometry() == null ? null : ruta.getGeometry().toString();
		return new TrayectoDTO((int) Math.round(ruta.getDistance()), (int) Math.round(ruta.getDuration()), geometria,
				pasos);
	}

	/**
	 * Describe un paso con los datos de OSRM, por ejemplo "turn left Carrera 15 (1200 m)".
	 *
	 * @param p paso de OSRM
	 * @return descripción del paso
	 */
	private String describir(OsrmPasoDTO p) {
		StringBuilder sb = new StringBuilder();
		if (p.getManeuver() != null) {
			sb.append(p.getManeuver().getType());
			if (p.getManeuver().getModifier() != null) {
				sb.append(" ").append(p.getManeuver().getModifier());
			}
		}
		if (p.getName() != null && !p.getName().isBlank()) {
			sb.append(" ").append(p.getName());
		}
		return sb.append(" (").append(Math.round(p.getDistance())).append(" m)").toString().trim();
	}
}
