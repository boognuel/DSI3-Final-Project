package co.edu.unbosque.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.dto.LugarDTO;
import co.edu.unbosque.dto.RecomendacionDTO;
import co.edu.unbosque.dto.ZonaDTO;
import co.edu.unbosque.exception.DatosInvalidosException;

/**
 * Pruebas de RecomendacionService con los servicios del catálogo simulados.
 */
@ExtendWith(MockitoExtension.class)
class RecomendacionServiceTest {
	@Mock
	private ZonaService zonaService;
	@Mock
	private LugarService lugarService;
	@InjectMocks
	private RecomendacionService recomendacionService;

	/**
	 * Un presupuesto negativo se rechaza con 400 sin consultar el catálogo.
	 */
	@Test
	void rechazaPresupuestoNegativo() {
		assertThrows(DatosInvalidosException.class,
				() -> recomendacionService.recomendar(new BigDecimal("-1"), null));
		verify(lugarService, never()).recomendar(any(), any());
	}

	/**
	 * La recomendación junta las zonas y los lugares a los que alcanza el presupuesto.
	 */
	@Test
	void juntaZonasYLugaresDelPresupuesto() {
		BigDecimal presupuesto = new BigDecimal("5000");
		List<ZonaDTO> zonas = List.of(new ZonaDTO());
		List<LugarDTO> lugares = List.of(new LugarDTO(), new LugarDTO());
		when(zonaService.alcanzables(presupuesto)).thenReturn(zonas);
		when(lugarService.recomendar(presupuesto, List.of(1L))).thenReturn(lugares);
		RecomendacionDTO r = recomendacionService.recomendar(presupuesto, List.of(1L));
		assertEquals(presupuesto, r.getPresupuesto());
		assertEquals(zonas, r.getZonas());
		assertEquals(lugares, r.getLugares());
	}
}
