package co.edu.unbosque.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.CategoriaDTO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Categoria;
import co.edu.unbosque.repository.CategoriaRepository;
import co.edu.unbosque.repository.LugarRepository;

/**
 * Gestión de las categorías de planes (bailar, comer, tomar, jugar, etc.).
 */
@Service
public class CategoriaService implements CRUDOperation<CategoriaDTO> {
	@Autowired
	private CategoriaRepository categoriaRepo;
	@Autowired
	private LugarRepository lugarRepo;

	/**
	 * Crea una categoría y deja su identificador en el DTO.
	 *
	 * @param data datos de la categoría
	 * @throws ConflictoException si ya existe una categoría con ese nombre
	 */
	@Override
	public void create(CategoriaDTO data) {
		String nombre = normalizar(data.getNombre());
		if (categoriaRepo.existsByNombreIgnoreCase(nombre)) {
			throw new ConflictoException("Ya existe una categoría con ese nombre");
		}
		Categoria c = new Categoria();
		copiar(data, c, nombre);
		categoriaRepo.save(c);
		data.setId(c.getId());
	}

	/**
	 * Lista las categorías en orden alfabético.
	 *
	 * @return categorías
	 */
	@Override
	public List<CategoriaDTO> getAll() {
		return categoriaRepo.findAllByOrderByNombreAsc().stream().map(this::toDTO).toList();
	}

	/**
	 * Elimina una categoría que no tenga lugares.
	 *
	 * @param id identificador de la categoría
	 * @throws RecursoNoEncontradoException si la categoría no existe
	 * @throws ConflictoException           si la categoría tiene lugares
	 */
	@Override
	public void deleteById(Long id) {
		Categoria c = buscar(id);
		if (lugarRepo.existsByCategoriaId(id)) {
			throw new ConflictoException("La categoría tiene lugares; cámbialos de categoría antes de eliminarla");
		}
		categoriaRepo.delete(c);
	}

	/**
	 * Actualiza el nombre y la descripción de una categoría.
	 *
	 * @param id      identificador de la categoría
	 * @param newData datos nuevos
	 * @throws RecursoNoEncontradoException si la categoría no existe
	 * @throws ConflictoException           si otra categoría ya usa ese nombre
	 */
	@Override
	public void updateById(Long id, CategoriaDTO newData) {
		Categoria c = buscar(id);
		String nombre = normalizar(newData.getNombre());
		if (categoriaRepo.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
			throw new ConflictoException("Ya existe una categoría con ese nombre");
		}
		copiar(newData, c, nombre);
		categoriaRepo.save(c);
	}

	/**
	 * Cuenta las categorías.
	 *
	 * @return número de categorías
	 */
	@Override
	public long count() {
		return categoriaRepo.count();
	}

	/**
	 * Indica si existe una categoría.
	 *
	 * @param id identificador de la categoría
	 * @return {@code true} si existe
	 */
	@Override
	public boolean exist(Long id) {
		return categoriaRepo.existsById(id);
	}

	/**
	 * Obtiene una categoría por su identificador.
	 *
	 * @param id identificador de la categoría
	 * @return datos de la categoría
	 * @throws RecursoNoEncontradoException si la categoría no existe
	 */
	public CategoriaDTO getById(Long id) {
		return toDTO(buscar(id));
	}

	/**
	 * Busca la entidad de una categoría.
	 *
	 * @param id identificador de la categoría
	 * @return la categoría
	 * @throws RecursoNoEncontradoException si no existe
	 */
	private Categoria buscar(Long id) {
		return categoriaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
	}

	/**
	 * Quita los espacios sobrantes y pasa el nombre a minúsculas, para que "Comer" y "comer " sean
	 * la misma categoría.
	 *
	 * @param nombre nombre recibido
	 * @return nombre normalizado
	 */
	private String normalizar(String nombre) {
		return nombre.trim().toLowerCase();
	}

	/**
	 * Copia los datos del DTO a la entidad.
	 *
	 * @param d      datos recibidos
	 * @param c      categoría que se modifica
	 * @param nombre nombre ya normalizado
	 */
	private void copiar(CategoriaDTO d, Categoria c, String nombre) {
		c.setNombre(nombre);
		c.setDescripcion(d.getDescripcion());
	}

	/**
	 * Convierte la entidad en DTO.
	 *
	 * @param c categoría
	 * @return datos de la categoría
	 */
	private CategoriaDTO toDTO(Categoria c) {
		CategoriaDTO d = new CategoriaDTO();
		d.setId(c.getId());
		d.setNombre(c.getNombre());
		d.setDescripcion(c.getDescripcion());
		return d;
	}
}
