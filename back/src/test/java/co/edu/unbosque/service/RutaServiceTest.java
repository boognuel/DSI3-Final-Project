package co.edu.unbosque.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.dto.RutaDTO;
import co.edu.unbosque.exception.AccesoDenegadoException;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.exception.ServicioExternoException;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.Lugar;
import co.edu.unbosque.model.ModoTransporte;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.Salida;
import co.edu.unbosque.model.SalidaLugar;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.RutaRepository;
import co.edu.unbosque.repository.SalidaLugarRepository;
import co.edu.unbosque.repository.SalidaRepository;

/**
 * Pruebas de RutaService con repositorios, OSRM y Groq simulados.
 */
@ExtendWith(MockitoExtension.class)
class RutaServiceTest {
	@Mock
	private SalidaRepository salidaRepo;
	@Mock
	private SalidaLugarRepository salidaLugarRepo;
	@Mock
	private RutaRepository rutaRepo;
	@Mock
	private SalidaService salidaService;
	@Mock
	private OsrmService osrmService;
	@Mock
	private GroqService groqService;
	@Mock
	private AuditoriaService auditoriaService;
	@InjectMocks
	private RutaService rutaService;

	private User usuario;
	private Lugar lugar;
	private OsrmService.Trayecto trayecto;

	/**
	 * Prepara una salida 4 con el lugar 7 propuesto y un usuario sin rol de administrador.
	 */
	@BeforeEach
	void setUp() {
		usuario = new User();
		usuario.setId(1L);
		Salida salida = new Salida();
		salida.setId(4L);
		lugar = new Lugar();
		lugar.setId(7L);
		lugar.setNombre("Panadería La Esquina");
		lugar.setDireccion("Calle 119 # 6-20");
		lugar.setLatitud(4.695);
		lugar.setLongitud(-74.0305);
		SalidaLugar sl = new SalidaLugar();
		sl.setLugar(lugar);
		when(salidaRepo.findById(4L)).thenReturn(Optional.of(salida));
		lenient().when(salidaLugarRepo.findBySalidaIdAndLugarId(4L, 7L))
				.thenReturn(Optional.of(sl));
		trayecto = new OsrmService.Trayecto(4120, 610, "{\"type\":\"LineString\"}", List.of("depart"));
	}

	/**
	 * Un participante recibe la ruta con las indicaciones de Groq y la ruta queda guardada.
	 */
	@Test
	void participanteRecibeRutaConIndicaciones() {
		when(salidaService.esParticipante(4L, usuario)).thenReturn(true);
		when(osrmService.calcular(ModoTransporte.PIE, 4.668, -74.056, 4.695, -74.0305)).thenReturn(trayecto);
		when(groqService.redactarIndicaciones("Panadería La Esquina, Calle 119 # 6-20", ModoTransporte.PIE, trayecto))
				.thenReturn("1. Camina por la Calle 85.");
		RutaDTO r = rutaService.calcular(peticion(ModoTransporte.PIE), usuario);
		assertEquals(4120, r.getDistanciaMetros());
		assertEquals(10, r.getDuracionMinutos());
		assertEquals("{\"type\":\"LineString\"}", r.getGeometriaGeoJson());
		assertEquals("1. Camina por la Calle 85.", r.getIndicaciones());
		verify(rutaRepo).save(any());
	}

	/**
	 * Si Groq no responde, la ruta se entrega igual con un aviso en vez de las indicaciones.
	 */
	@Test
	void sinGroqLaRutaSeEntregaIgual() {
		when(salidaService.esParticipante(4L, usuario)).thenReturn(true);
		when(osrmService.calcular(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(trayecto);
		RutaDTO r = rutaService.calcular(peticion(null), usuario);
		assertEquals(ModoTransporte.CARRO, r.getModoTransporte());
		assertEquals(RutaService.SIN_INDICACIONES, r.getIndicaciones());
	}

	/**
	 * Quien no participa en la salida no puede calcular rutas en ella.
	 */
	@Test
	void noParticipanteRecibe403() {
		when(salidaService.esParticipante(4L, usuario)).thenReturn(false);
		assertThrows(AccesoDenegadoException.class, () -> rutaService.calcular(peticion(null), usuario));
		verify(osrmService, never()).calcular(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
	}

	/**
	 * Solo se calculan rutas a lugares propuestos en la salida.
	 */
	@Test
	void lugarNoPropuestoSeRechaza() {
		when(salidaService.esParticipante(4L, usuario)).thenReturn(true);
		RutaDTO d = peticion(null);
		d.setLugarId(99L);
		assertThrows(DatosInvalidosException.class, () -> rutaService.calcular(d, usuario));
	}

	/**
	 * Si OSRM falla, no se guarda la ruta y la falla queda en la auditoría.
	 */
	@Test
	void fallaDeOsrmSeAudita() {
		when(salidaService.esParticipante(4L, usuario)).thenReturn(true);
		when(osrmService.calcular(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.thenThrow(new ServicioExternoException("caído"));
		assertThrows(ServicioExternoException.class, () -> rutaService.calcular(peticion(null), usuario));
		verify(rutaRepo, never()).save(any());
		verify(auditoriaService).registrar(eq(usuario), eq(AccionAuditoria.CALCULAR_RUTA), eq("Lugar"), eq(7L),
				anyString(), eq(ResultadoAuditoria.FALLO));
	}

	/**
	 * Crea una petición desde la Calle 85 con Carrera 15 hacia el lugar 7.
	 *
	 * @param modo modo de transporte, o nulo
	 * @return datos de la petición
	 */
	private RutaDTO peticion(ModoTransporte modo) {
		RutaDTO d = new RutaDTO();
		d.setSalidaId(4L);
		d.setLugarId(7L);
		d.setOrigenLatitud(4.668);
		d.setOrigenLongitud(-74.056);
		d.setModoTransporte(modo);
		return d;
	}
}
