package co.edu.unbosque.bogoparche.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.bogoparche.dto.LugarDTO;
import co.edu.unbosque.bogoparche.exception.ConflictoException;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.RecursoNoEncontradoException;
import co.edu.unbosque.bogoparche.model.Categoria;
import co.edu.unbosque.bogoparche.model.Lugar;
import co.edu.unbosque.bogoparche.model.Zona;
import co.edu.unbosque.bogoparche.repository.CategoriaRepository;
import co.edu.unbosque.bogoparche.repository.LugarRepository;
import co.edu.unbosque.bogoparche.repository.RutaRepository;
import co.edu.unbosque.bogoparche.repository.SalidaLugarRepository;
import co.edu.unbosque.bogoparche.repository.ZonaRepository;

/**
 * Pruebas de las reglas de LugarService con repositorios simulados: no se usa la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class LugarServiceTest {
	@Mock
	private LugarRepository lugarRepo;
	@Mock
	private CategoriaRepository categoriaRepo;
	@Mock
	private ZonaRepository zonaRepo;
	@Mock
	private SalidaLugarRepository salidaLugarRepo;
	@Mock
	private RutaRepository rutaRepo;
	@InjectMocks
	private LugarService lugarService;

	private Zona zona;
	private Categoria categoria;

	/**
	 * Prepara una zona con rango de 5.000 a 30.000 y la categoría "comer".
	 */
	@BeforeEach
	void setUp() {
		zona = new Zona();
		zona.setId(1L);
		zona.setNombre("Usaquén");
		zona.setGastoMinimo(new BigDecimal("5000"));
		zona.setGastoMaximo(new BigDecimal("30000"));
		categoria = new Categoria();
		categoria.setId(2L);
		categoria.setNombre("comer");
	}

	/**
	 * Un lugar válido se guarda con su categoría y su zona.
	 */
	@Test
	void creaLugarValido() {
		when(categoriaRepo.findById(2L)).thenReturn(Optional.of(categoria));
		when(zonaRepo.findById(1L)).thenReturn(Optional.of(zona));
		LugarDTO dto = lugar("12000", 4.6950, -74.0300);
		lugarService.create(dto);
		verify(lugarRepo).save(any(Lugar.class));
	}

	/**
	 * Un costo por fuera del rango de la zona se rechaza con 400.
	 */
	@Test
	void rechazaCostoFueraDelRangoDeLaZona() {
		when(categoriaRepo.findById(2L)).thenReturn(Optional.of(categoria));
		when(zonaRepo.findById(1L)).thenReturn(Optional.of(zona));
		DatosInvalidosException e = assertThrows(DatosInvalidosException.class,
				() -> lugarService.create(lugar("45000", 4.6950, -74.0300)));
		assertEquals("El costo del lugar debe estar entre 5000 y 30000, el rango de gasto de la zona Usaquén",
				e.getMessage());
		verify(lugarRepo, never()).save(any());
	}

	/**
	 * Un lugar al sur de la calle 75 se rechaza antes de consultar la base de datos.
	 */
	@Test
	void rechazaLugarAlSurDeLaCalle75() {
		assertThrows(DatosInvalidosException.class, () -> lugarService.create(lugar("12000", 4.6280, -74.0650)));
		verify(lugarRepo, never()).save(any());
	}

	/**
	 * Si la zona no existe, responde 404.
	 */
	@Test
	void rechazaZonaInexistente() {
		when(categoriaRepo.findById(2L)).thenReturn(Optional.of(categoria));
		when(zonaRepo.findById(1L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> lugarService.create(lugar("12000", 4.6950, -74.0300)));
	}

	/**
	 * Un lugar propuesto en alguna salida no se puede eliminar.
	 */
	@Test
	void noEliminaLugarUsadoEnSalidas() {
		when(lugarRepo.findById(7L)).thenReturn(Optional.of(new Lugar()));
		when(salidaLugarRepo.existsByLugarId(7L)).thenReturn(true);
		assertThrows(ConflictoException.class, () -> lugarService.deleteById(7L));
		verify(lugarRepo, never()).delete(any());
	}

	/**
	 * Crea un lugar de prueba en la zona y la categoría preparadas.
	 *
	 * @param costo    costo por persona
	 * @param latitud  latitud
	 * @param longitud longitud
	 * @return datos del lugar
	 */
	private LugarDTO lugar(String costo, double latitud, double longitud) {
		LugarDTO d = new LugarDTO();
		d.setNombre("Panadería La Esquina");
		d.setDireccion("Calle 119 # 6-20");
		d.setCategoriaId(2L);
		d.setZonaId(1L);
		d.setLatitud(latitud);
		d.setLongitud(longitud);
		d.setCostoEstimado(new BigDecimal(costo));
		return d;
	}
}
