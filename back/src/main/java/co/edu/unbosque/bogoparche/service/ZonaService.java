package co.edu.unbosque.bogoparche.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.ZonaDTO;
import co.edu.unbosque.bogoparche.exception.ConflictoException;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.RecursoNoEncontradoException;
import co.edu.unbosque.bogoparche.model.Zona;
import co.edu.unbosque.bogoparche.repository.LugarRepository;
import co.edu.unbosque.bogoparche.repository.ZonaRepository;
import co.edu.unbosque.bogoparche.util.UbicacionValidator;

/**
 * Gestión de las zonas de la ciudad y su rango de gasto por persona.
 */
@Service
public class ZonaService implements CRUDOperation<ZonaDTO> {
	@Autowired
	private ZonaRepository zonaRepo;
	@Autowired
	private LugarRepository lugarRepo;

	/**
	 * Crea una zona y deja su identificador en el DTO.
	 *
	 * @param data datos de la zona
	 * @throws DatosInvalidosException si el gasto mínimo supera al máximo
	 * @throws ConflictoException      si ya existe una zona con ese nombre
	 */
	@Override
	public void create(ZonaDTO data) {
		validarRango(data);
		String nombre = data.getNombre().trim();
		if (zonaRepo.existsByNombreIgnoreCase(nombre)) {
			throw new ConflictoException("Ya existe una zona con ese nombre");
		}
		Zona z = new Zona();
		copiar(data, z, nombre);
		zonaRepo.save(z);
		data.setId(z.getId());
	}

	/**
	 * Lista las zonas en orden alfabético.
	 *
	 * @return zonas
	 */
	@Override
	public List<ZonaDTO> getAll() {
		return zonaRepo.findAllByOrderByNombreAsc().stream().map(this::toDTO).toList();
	}

	/**
	 * Elimina una zona que no tenga lugares.
	 *
	 * @param id identificador de la zona
	 * @throws RecursoNoEncontradoException si la zona no existe
	 * @throws ConflictoException           si la zona tiene lugares
	 */
	@Override
	public void deleteById(Long id) {
		Zona z = buscar(id);
		if (lugarRepo.existsByZonaId(id)) {
			throw new ConflictoException("La zona tiene lugares; cámbialos de zona antes de eliminarla");
		}
		zonaRepo.delete(z);
	}

	/**
	 * Actualiza una zona. El nuevo rango de gasto debe seguir cubriendo el costo de sus lugares.
	 *
	 * @param id      identificador de la zona
	 * @param newData datos nuevos
	 * @throws RecursoNoEncontradoException si la zona no existe
	 * @throws DatosInvalidosException      si el gasto mínimo supera al máximo o algún lugar queda
	 *                                      fuera del nuevo rango
	 * @throws ConflictoException           si otra zona ya usa ese nombre
	 */
	@Override
	public void updateById(Long id, ZonaDTO newData) {
		Zona z = buscar(id);
		validarRango(newData);
		String nombre = newData.getNombre().trim();
		if (zonaRepo.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
			throw new ConflictoException("Ya existe una zona con ese nombre");
		}
		if (lugarRepo.existsByZonaIdAndCostoEstimadoLessThan(id, newData.getGastoMinimo())
				|| lugarRepo.existsByZonaIdAndCostoEstimadoGreaterThan(id, newData.getGastoMaximo())) {
			throw new DatosInvalidosException("Hay lugares de la zona con un costo fuera del nuevo rango");
		}
		copiar(newData, z, nombre);
		zonaRepo.save(z);
	}

	/**
	 * Cuenta las zonas.
	 *
	 * @return número de zonas
	 */
	@Override
	public long count() {
		return zonaRepo.count();
	}

	/**
	 * Indica si existe una zona.
	 *
	 * @param id identificador de la zona
	 * @return {@code true} si existe
	 */
	@Override
	public boolean exist(Long id) {
		return zonaRepo.existsById(id);
	}

	/**
	 * Obtiene una zona por su identificador.
	 *
	 * @param id identificador de la zona
	 * @return datos de la zona
	 * @throws RecursoNoEncontradoException si la zona no existe
	 */
	public ZonaDTO getById(Long id) {
		return toDTO(buscar(id));
	}

	/**
	 * Busca la entidad de una zona.
	 *
	 * @param id identificador de la zona
	 * @return la zona
	 * @throws RecursoNoEncontradoException si no existe
	 */
	private Zona buscar(Long id) {
		return zonaRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Zona no encontrada"));
	}

	/**
	 * Valida que el gasto mínimo no supere al máximo.
	 *
	 * @param d datos de la zona
	 * @throws DatosInvalidosException si el rango no es válido
	 */
	private void validarRango(ZonaDTO d) {
		if (d.getGastoMinimo().compareTo(d.getGastoMaximo()) > 0) {
			throw new DatosInvalidosException("El gasto mínimo no puede ser mayor que el gasto máximo");
		}
		if (d.getLatitudMinima() == null && d.getLatitudMaxima() == null) {
			return;
		}
		if (d.getLatitudMinima() == null || d.getLatitudMaxima() == null
				|| d.getLatitudMinima() >= d.getLatitudMaxima()
				|| d.getLatitudMinima() < UbicacionValidator.LATITUD_MINIMA
				|| d.getLatitudMaxima() > UbicacionValidator.LATITUD_MAXIMA) {
			throw new DatosInvalidosException("Las latitudes de la zona deben ir juntas, la mínima menor que la máxima, "
					+ "y estar entre " + UbicacionValidator.LATITUD_MINIMA + " y " + UbicacionValidator.LATITUD_MAXIMA);
		}
	}

	/**
	 * Copia los datos del DTO a la entidad.
	 *
	 * @param d      datos recibidos
	 * @param z      zona que se modifica
	 * @param nombre nombre sin espacios sobrantes
	 */
	private void copiar(ZonaDTO d, Zona z, String nombre) {
		z.setNombre(nombre);
		z.setDescripcion(d.getDescripcion());
		z.setGastoMinimo(d.getGastoMinimo());
		z.setGastoMaximo(d.getGastoMaximo());
		z.setLatitudMinima(d.getLatitudMinima());
		z.setLatitudMaxima(d.getLatitudMaxima());
	}

	/**
	 * Convierte la entidad en DTO.
	 *
	 * @param z zona
	 * @return datos de la zona
	 */
	private ZonaDTO toDTO(Zona z) {
		ZonaDTO d = new ZonaDTO();
		d.setId(z.getId());
		d.setNombre(z.getNombre());
		d.setDescripcion(z.getDescripcion());
		d.setGastoMinimo(z.getGastoMinimo());
		d.setGastoMaximo(z.getGastoMaximo());
		d.setLatitudMinima(z.getLatitudMinima());
		d.setLatitudMaxima(z.getLatitudMaxima());
		return d;
	}
}
