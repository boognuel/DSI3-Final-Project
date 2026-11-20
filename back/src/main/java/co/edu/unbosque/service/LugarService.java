package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.LugarDTO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Categoria;
import co.edu.unbosque.model.Lugar;
import co.edu.unbosque.model.Zona;
import co.edu.unbosque.repository.CategoriaRepository;
import co.edu.unbosque.repository.LugarRepository;
import co.edu.unbosque.repository.RutaRepository;
import co.edu.unbosque.repository.SalidaLugarRepository;
import co.edu.unbosque.repository.ZonaRepository;
import co.edu.unbosque.util.UbicacionValidator;

/**
 * Gestión de los lugares que crea el equipo. Cada lugar pertenece a una categoría y a una zona, y
 * su costo por persona debe estar dentro del rango de gasto de la zona.
 */
@Service
public class LugarService implements CRUDOperation<LugarDTO> {
	@Autowired
	private LugarRepository lugarRepo;
	@Autowired
	private CategoriaRepository categoriaRepo;
	@Autowired
	private ZonaRepository zonaRepo;
	@Autowired
	private SalidaLugarRepository salidaLugarRepo;
	@Autowired
	private RutaRepository rutaRepo;

	/**
	 * Crea un lugar y deja su identificador en el DTO.
	 *
	 * @param data datos del lugar
	 * @throws RecursoNoEncontradoException si la categoría o la zona no existen
	 * @throws DatosInvalidosException      si la ubicación está fuera del área o el costo está fuera
	 *                                      del rango de la zona
	 */
	@Override
	public void create(LugarDTO data) {
		Lugar l = new Lugar();
		copiar(data, l);
		lugarRepo.save(l);
		data.setId(l.getId());
	}

	/**
	 * Lista todos los lugares, del más económico al más costoso.
	 *
	 * @return lugares
	 */
	@Override
	public List<LugarDTO> getAll() {
		return filtrar(null, null);
	}

	/**
	 * Lista los lugares con filtros opcionales, del más económico al más costoso.
	 *
	 * @param zonaId      zona; nula para todas
	 * @param categoriaId categoría; nula para todas
	 * @return lugares que cumplen los filtros
	 */
	public List<LugarDTO> filtrar(Long zonaId, Long categoriaId) {
		return lugarRepo.buscar(zonaId, categoriaId).stream().map(this::toDTO).toList();
	}

	/**
	 * Lista los lugares que caben en el presupuesto, del más económico al más costoso. Si se indican
	 * categorías, solo se incluyen los lugares de esas categorías.
	 *
	 * @param presupuesto  dinero por persona
	 * @param categoriaIds categorías que le gustan al usuario; nula o vacía para todas
	 * @return lugares recomendados
	 */
	public List<LugarDTO> recomendar(BigDecimal presupuesto, Collection<Long> categoriaIds) {
		List<Lugar> lugares = categoriaIds == null || categoriaIds.isEmpty() ? lugarRepo.alcanzables(presupuesto)
				: lugarRepo.alcanzablesEnCategorias(presupuesto, categoriaIds);
		return lugares.stream().map(this::toDTO).toList();
	}

	/**
	 * Elimina un lugar que no esté propuesto en ninguna salida ni tenga rutas calculadas.
	 *
	 * @param id identificador del lugar
	 * @throws RecursoNoEncontradoException si el lugar no existe
	 * @throws ConflictoException           si el lugar se usa en alguna salida o ruta
	 */
	@Override
	public void deleteById(Long id) {
		Lugar l = buscar(id);
		if (salidaLugarRepo.existsByLugarId(id) || rutaRepo.existsByLugarDestinoId(id)) {
			throw new ConflictoException("El lugar se usa en salidas o rutas y no se puede eliminar");
		}
		lugarRepo.delete(l);
	}

	/**
	 * Actualiza un lugar.
	 *
	 * @param id      identificador del lugar
	 * @param newData datos nuevos
	 * @throws RecursoNoEncontradoException si el lugar, la categoría o la zona no existen
	 * @throws DatosInvalidosException      si la ubicación está fuera del área o el costo está fuera
	 *                                      del rango de la zona
	 */
	@Override
	public void updateById(Long id, LugarDTO newData) {
		Lugar l = buscar(id);
		copiar(newData, l);
		lugarRepo.save(l);
	}

	/**
	 * Cuenta los lugares.
	 *
	 * @return número de lugares
	 */
	@Override
	public long count() {
		return lugarRepo.count();
	}

	/**
	 * Indica si existe un lugar.
	 *
	 * @param id identificador del lugar
	 * @return {@code true} si existe
	 */
	@Override
	public boolean exist(Long id) {
		return lugarRepo.existsById(id);
	}

	/**
	 * Obtiene un lugar por su identificador.
	 *
	 * @param id identificador del lugar
	 * @return datos del lugar
	 * @throws RecursoNoEncontradoException si el lugar no existe
	 */
	public LugarDTO getById(Long id) {
		return toDTO(buscar(id));
	}

	/**
	 * Busca la entidad de un lugar.
	 *
	 * @param id identificador del lugar
	 * @return el lugar
	 * @throws RecursoNoEncontradoException si no existe
	 */
	private Lugar buscar(Long id) {
		return lugarRepo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Lugar no encontrado"));
	}

	/**
	 * Valida los datos y los copia a la entidad.
	 *
	 * @param d datos recibidos
	 * @param l lugar que se modifica
	 * @throws RecursoNoEncontradoException si la categoría o la zona no existen
	 * @throws DatosInvalidosException      si la ubicación está fuera del área o el costo está fuera
	 *                                      del rango de la zona
	 */
	private void copiar(LugarDTO d, Lugar l) {
		UbicacionValidator.validar(d.getLatitud(), d.getLongitud());
		Categoria categoria = categoriaRepo.findById(d.getCategoriaId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
		Zona zona = zonaRepo.findById(d.getZonaId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Zona no encontrada"));
		if (d.getCostoEstimado().compareTo(zona.getGastoMinimo()) < 0
				|| d.getCostoEstimado().compareTo(zona.getGastoMaximo()) > 0) {
			throw new DatosInvalidosException("El costo del lugar debe estar entre " + zona.getGastoMinimo()
					+ " y " + zona.getGastoMaximo() + ", el rango de gasto de la zona " + zona.getNombre());
		}
		l.setNombre(d.getNombre().trim());
		l.setDescripcion(d.getDescripcion());
		l.setCategoria(categoria);
		l.setZona(zona);
		l.setDireccion(d.getDireccion().trim());
		l.setLatitud(d.getLatitud());
		l.setLongitud(d.getLongitud());
		l.setCostoEstimado(d.getCostoEstimado());
	}

	/**
	 * Convierte la entidad en DTO, con los nombres de la categoría y la zona.
	 *
	 * @param l lugar
	 * @return datos del lugar
	 */
	private LugarDTO toDTO(Lugar l) {
		LugarDTO d = new LugarDTO();
		d.setId(l.getId());
		d.setNombre(l.getNombre());
		d.setDescripcion(l.getDescripcion());
		if (l.getCategoria() != null) {
			d.setCategoriaId(l.getCategoria().getId());
			d.setCategoriaNombre(l.getCategoria().getNombre());
		}
		if (l.getZona() != null) {
			d.setZonaId(l.getZona().getId());
			d.setZonaNombre(l.getZona().getNombre());
		}
		d.setDireccion(l.getDireccion());
		d.setLatitud(l.getLatitud());
		d.setLongitud(l.getLongitud());
		d.setCostoEstimado(l.getCostoEstimado());
		return d;
	}
}
