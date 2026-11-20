package co.edu.unbosque.service;

import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.RutaDTO;
import co.edu.unbosque.exception.AccesoDenegadoException;
import co.edu.unbosque.exception.ApiException;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.exception.ServicioExternoException;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.Lugar;
import co.edu.unbosque.model.ModoTransporte;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.Ruta;
import co.edu.unbosque.model.Salida;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.RutaRepository;
import co.edu.unbosque.repository.SalidaLugarRepository;
import co.edu.unbosque.repository.SalidaRepository;

/**
 * Calcula la ruta desde la ubicación del usuario hasta un lugar propuesto en una salida: OSRM da el
 * camino y Groq redacta las indicaciones. Si Groq falla, la ruta se entrega igual, sin indicaciones.
 */
@Service
public class RutaService {
	static final String SIN_INDICACIONES = "Las indicaciones no están disponibles en este momento; "
			+ "sigue la ruta marcada en el mapa.";

	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private SalidaLugarRepository salidaLugarRepo;
	@Autowired
	private RutaRepository rutaRepo;
	@Autowired
	private SalidaService salidaService;
	@Autowired
	private OsrmService osrmService;
	@Autowired
	private GroqService groqService;
	@Autowired
	private AuditoriaService auditoriaService;

	/**
	 * Calcula la ruta, la guarda y registra la auditoría.
	 *
	 * @param d datos de la petición
	 * @param u usuario autenticado
	 * @return la petición completada con distancia, duración, línea del camino e indicaciones
	 * @throws RecursoNoEncontradoException si la salida no existe
	 * @throws AccesoDenegadoException      si el usuario no participa en la salida ni es administrador
	 * @throws DatosInvalidosException      si el lugar no está propuesto en la salida o no hay camino
	 * @throws ServicioExternoException     si el servicio de rutas no responde
	 */
	public RutaDTO calcular(RutaDTO d, User u) {
		Salida salida = salidaRepo.findById(d.getSalidaId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Salida no encontrada"));
		if (u.getRole() != User.Role.ADMIN && !salidaService.esParticipante(salida.getId(), u)) {
			throw new AccesoDenegadoException("No participas en esta salida");
		}
		Lugar lugar = salidaLugarRepo.findBySalidaIdAndLugarId(salida.getId(), d.getLugarId())
				.orElseThrow(() -> new DatosInvalidosException("El lugar no está propuesto en esta salida")).getLugar();
		ModoTransporte modo = d.getModoTransporte() == null ? ModoTransporte.CARRO : d.getModoTransporte();

		OsrmService.Trayecto trayecto;
		try {
			trayecto = osrmService.calcular(modo, d.getOrigenLatitud(), d.getOrigenLongitud(), lugar.getLatitud(),
					lugar.getLongitud());
		} catch (ApiException e) {
			auditoriaService.registrar(u, AccionAuditoria.CALCULAR_RUTA, "Lugar", lugar.getId(),
					"Ruta a " + lugar.getNombre() + ": " + e.getMessage(), ResultadoAuditoria.FALLO);
			throw e;
		}

		Ruta r = new Ruta();
		r.setSalida(salida);
		r.setUsuario(u);
		r.setLugarDestino(lugar);
		r.setOrigenLatitud(d.getOrigenLatitud());
		r.setOrigenLongitud(d.getOrigenLongitud());
		r.setDistanciaMetros(trayecto.distanciaMetros());
		r.setDuracionSegundos(trayecto.duracionSegundos());
		r.setModoTransporte(modo);
		r.setFechaCalculo(LocalDateTime.now());
		rutaRepo.save(r);

		String indicaciones = groqService.redactarIndicaciones(lugar.getNombre() + ", " + lugar.getDireccion(), modo,
				trayecto);
		auditoriaService.registrar(u, AccionAuditoria.CALCULAR_RUTA, "Ruta", r.getId(),
				"Ruta a " + lugar.getNombre() + " en " + modo.name().toLowerCase(Locale.ROOT)
						+ (indicaciones == null ? ", sin indicaciones" : ""),
				ResultadoAuditoria.EXITO);

		d.setModoTransporte(modo);
		d.setDestinoNombre(lugar.getNombre());
		d.setDistanciaMetros(trayecto.distanciaMetros());
		d.setDuracionSegundos(trayecto.duracionSegundos());
		d.setDuracionMinutos((int) Math.round(trayecto.duracionSegundos() / 60.0));
		d.setGeometriaGeoJson(trayecto.geometriaGeoJson());
		d.setIndicaciones(indicaciones == null ? SIN_INDICACIONES : indicaciones);
		return d;
	}
}
