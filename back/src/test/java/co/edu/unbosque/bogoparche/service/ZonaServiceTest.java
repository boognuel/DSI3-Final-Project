package co.edu.unbosque.bogoparche.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.bogoparche.dto.ZonaDTO;
import co.edu.unbosque.bogoparche.exception.ConflictoException;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.model.Zona;
import co.edu.unbosque.bogoparche.repository.LugarRepository;
import co.edu.unbosque.bogoparche.repository.ZonaRepository;

/**
 * Pruebas de las reglas de ZonaService con repositorios simulados: no se usa la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {
	@Mock
	private ZonaRepository zonaRepo;
	@Mock
	private LugarRepository lugarRepo;
	@InjectMocks
	private ZonaService zonaService;

	/**
	 * Un gasto mínimo mayor que el máximo se rechaza con 400.
	 */
	@Test
	void rechazaRangoInvertido() {
		assertThrows(DatosInvalidosException.class, () -> zonaService.create(zona("Chicó", "40000", "20000")));
		verify(zonaRepo, never()).save(any());
	}

	/**
	 * Un nombre repetido, aunque cambien las mayúsculas, se rechaza con 409.
	 */
	@Test
	void rechazaNombreRepetido() {
		when(zonaRepo.existsByNombreIgnoreCase("Chicó")).thenReturn(true);
		assertThrows(ConflictoException.class, () -> zonaService.create(zona(" Chicó ", "10000", "50000")));
	}

	/**
	 * No se puede reducir el rango si deja lugares de la zona por fuera.
	 */
	@Test
	void noReduceRangoSiDejaLugaresPorFuera() {
		when(zonaRepo.findById(3L)).thenReturn(Optional.of(new Zona()));
		when(lugarRepo.existsByZonaIdAndCostoEstimadoLessThan(3L, new BigDecimal("20000"))).thenReturn(true);
		assertThrows(DatosInvalidosException.class,
				() -> zonaService.updateById(3L, zona("Chicó", "20000", "50000")));
		verify(zonaRepo, never()).save(any());
	}

	/**
	 * Una zona con lugares no se puede eliminar.
	 */
	@Test
	void noEliminaZonaConLugares() {
		when(zonaRepo.findById(3L)).thenReturn(Optional.of(new Zona()));
		when(lugarRepo.existsByZonaId(3L)).thenReturn(true);
		assertThrows(ConflictoException.class, () -> zonaService.deleteById(3L));
		verify(zonaRepo, never()).delete(any());
	}

	/**
	 * Crea los datos de una zona de prueba.
	 *
	 * @param nombre nombre
	 * @param minimo gasto mínimo
	 * @param maximo gasto máximo
	 * @return datos de la zona
	 */
	private ZonaDTO zona(String nombre, String minimo, String maximo) {
		ZonaDTO d = new ZonaDTO();
		d.setNombre(nombre);
		d.setGastoMinimo(new BigDecimal(minimo));
		d.setGastoMaximo(new BigDecimal(maximo));
		return d;
	}
}
