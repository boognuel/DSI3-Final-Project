package co.edu.unboque.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unboque.dto.SalidaLugarDTO;
import co.edu.unboque.model.Lugar;
import co.edu.unboque.model.Salida;
import co.edu.unboque.model.SalidaLugar;
import co.edu.unboque.repository.LugarRepository;
import co.edu.unboque.repository.SalidaLugarRepository;
import co.edu.unboque.repository.SalidaRepository;

@Service
public class SalidaLugarService {
	@Autowired
	private SalidaLugarRepository salidaLugarRepo;
	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private LugarRepository lugarRepo;

	// 0 = ok, 1 = salida no existe, 2 = lugar no existe, 3 = ya agregado, 4 = el lugar supera el presupuesto
	public int agregar(Long salidaId, Long lugarId) {
		Optional<Salida> s = salidaRepo.findById(salidaId);
		if (s.isEmpty()) {
			return 1;
		}
		Optional<Lugar> l = lugarRepo.findById(lugarId);
		if (l.isEmpty()) {
			return 2;
		}
		if (salidaLugarRepo.findBySalidaIdAndLugarId(salidaId, lugarId).isPresent()) {
			return 3;
		}
		Lugar lugar = l.get();
		if (lugar.getCostoEstimado() != null && lugar.getCostoEstimado().compareTo(s.get().getPresupuesto()) > 0) {
			return 4;
		}
		SalidaLugar sl = new SalidaLugar();
		sl.setSalida(s.get());
		sl.setLugar(lugar);
		sl.setOrden(salidaLugarRepo.findBySalidaId(salidaId).size() + 1);
		sl.setSeleccionado(false);
		sl.setCostoEstimadoPorPersona(lugar.getCostoEstimado());
		salidaLugarRepo.save(sl);
		return 0;
	}

	public List<SalidaLugarDTO> listar(Long salidaId) {
		List<SalidaLugarDTO> lista = new ArrayList<>();
		salidaLugarRepo.findBySalidaId(salidaId).forEach(sl -> lista.add(toDTO(sl)));
		lista.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
		return lista;
	}

	// Marca un lugar como el destino elegido y desmarca los demás. 0 = ok, 1 = ese lugar no está en la salida
	public int seleccionar(Long salidaId, Long lugarId) {
		List<SalidaLugar> todos = salidaLugarRepo.findBySalidaId(salidaId);
		boolean existe = todos.stream().anyMatch(sl -> sl.getLugar().getId().equals(lugarId));
		if (!existe) {
			return 1;
		}
		todos.forEach(sl -> sl.setSeleccionado(sl.getLugar().getId().equals(lugarId)));
		salidaLugarRepo.saveAll(todos);
		return 0;
	}

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
