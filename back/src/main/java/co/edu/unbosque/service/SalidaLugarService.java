package co.edu.unbosque.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.SalidaLugarDTO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatosInvalidosException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Lugar;
import co.edu.unbosque.model.Salida;
import co.edu.unbosque.model.SalidaLugar;
import co.edu.unbosque.repository.LugarRepository;
import co.edu.unbosque.repository.SalidaLugarRepository;
import co.edu.unbosque.repository.SalidaRepository;

/**
 * Gestión de los lugares que se proponen como opciones de una salida.
 */
@Service
public class SalidaLugarService {
	@Autowired
	private SalidaLugarRepository salidaLugarRepo;
	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private LugarRepository lugarRepo;

	/**
	 * Agrega un lugar al final de la lista de opciones de la salida.
	 *
	 * @param salidaId identificador de la salida
	 * @param lugarId  identificador del lugar
	 * @throws DatosInvalidosException      si no se envía el lugar o su costo supera el presupuesto
	 *                                      de la salida
	 * @throws RecursoNoEncontradoException si la salida o el lugar no existen
	 * @throws ConflictoException           si el lugar ya está en la salida
	 */
	public void agregar(Long salidaId, Long lugarId) {
		if (lugarId == null) {
			throw new DatosInvalidosException("lugarId es obligatorio");
		}
		Salida salida = salidaRepo.findById(salidaId)
				.orElseThrow(() -> new RecursoNoEncontradoException("Salida no encontrada"));
		Lugar lugar = lugarRepo.findById(lugarId)
				.orElseThrow(() -> new RecursoNoEncontradoException("Lugar no encontrado"));
		if (salidaLugarRepo.findBySalidaIdAndLugarId(salidaId, lugarId).isPresent()) {
			throw new ConflictoException("El lugar ya está en la salida");
		}
		if (lugar.getCostoEstimado() != null && lugar.getCostoEstimado().compareTo(salida.getPresupuesto()) > 0) {
			throw new DatosInvalidosException("El lugar supera el presupuesto de la salida");
		}
		SalidaLugar sl = new SalidaLugar();
		sl.setSalida(salida);
		sl.setLugar(lugar);
		sl.setOrden(salidaLugarRepo.findBySalidaId(salidaId).size() + 1);
		sl.setSeleccionado(false);
		sl.setCostoEstimadoPorPersona(lugar.getCostoEstimado());
		salidaLugarRepo.save(sl);
	}

	/**
	 * Lista los lugares propuestos para la salida, en el orden en que se agregaron.
	 *
	 * @param salidaId identificador de la salida
	 * @return lugares de la salida
	 */
	public List<SalidaLugarDTO> listar(Long salidaId) {
		List<SalidaLugarDTO> lista = new ArrayList<>();
		salidaLugarRepo.findBySalidaId(salidaId).forEach(sl -> lista.add(toDTO(sl)));
		lista.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
		return lista;
	}

	/**
	 * Marca un lugar como el destino elegido de la salida y desmarca los demás.
	 *
	 * @param salidaId identificador de la salida
	 * @param lugarId  identificador del lugar elegido
	 * @throws RecursoNoEncontradoException si el lugar no está en la salida
	 */
	public void seleccionar(Long salidaId, Long lugarId) {
		List<SalidaLugar> todos = salidaLugarRepo.findBySalidaId(salidaId);
		boolean existe = todos.stream().anyMatch(sl -> sl.getLugar().getId().equals(lugarId));
		if (!existe) {
			throw new RecursoNoEncontradoException("Ese lugar no está en la salida");
		}
		todos.forEach(sl -> sl.setSeleccionado(sl.getLugar().getId().equals(lugarId)));
		salidaLugarRepo.saveAll(todos);
	}

	/**
	 * Convierte un lugar de la salida en DTO, con los datos del lugar para mostrarlo en el mapa.
	 *
	 * @param sl lugar de la salida
	 * @return datos del lugar de la salida
	 */
	private SalidaLugarDTO toDTO(SalidaLugar sl) {
		SalidaLugarDTO d = new SalidaLugarDTO();
		d.setId(sl.getId());
		d.setSalidaId(sl.getSalida().getId());
		d.setLugarId(sl.getLugar().getId());
		d.setLugarNombre(sl.getLugar().getNombre());
		d.setCategoria(sl.getLugar().getCategoria());
		d.setLatitud(sl.getLugar().getLatitud());
		d.setLongitud(sl.getLugar().getLongitud());
		d.setOrden(sl.getOrden());
		d.setSeleccionado(sl.isSeleccionado());
		d.setCostoEstimadoPorPersona(sl.getCostoEstimadoPorPersona());
		return d;
	}
}
