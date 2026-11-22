package co.edu.unbosque.bogoparche.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.bogoparche.dto.OverpassElementoDTO;
import co.edu.unbosque.bogoparche.dto.OverpassRespuestaDTO;
import co.edu.unbosque.bogoparche.exception.DatosInvalidosException;
import co.edu.unbosque.bogoparche.exception.ServicioExternoException;
import co.edu.unbosque.bogoparche.model.Categoria;
import co.edu.unbosque.bogoparche.model.Lugar;
import co.edu.unbosque.bogoparche.model.Zona;
import co.edu.unbosque.bogoparche.repository.CategoriaRepository;
import co.edu.unbosque.bogoparche.repository.LugarRepository;
import co.edu.unbosque.bogoparche.repository.ZonaRepository;
import co.edu.unbosque.bogoparche.util.UbicacionValidator;

/**
 * Carga los lugares del catálogo desde OpenStreetMap con la API de Overpass. Cada categoría indica
 * qué etiquetas de OpenStreetMap le corresponden y cada zona, entre qué latitudes queda. Como
 * OpenStreetMap no tiene precios, el costo de cada lugar nuevo es el precio de referencia de su
 * categoría, ajustado al rango de gasto de su zona, y queda marcado como estimado.
 */
@Service
public class OverpassService {
	private static final String SIN_DIRECCION = "Sin dirección registrada";

	@Autowired
	private LugarRepository lugarRepo;
	@Autowired
	private CategoriaRepository categoriaRepo;
	@Autowired
	private ZonaRepository zonaRepo;

	/**
	 * Consulta OpenStreetMap y agrega o actualiza los lugares de las categorías y zonas configuradas.
	 * Los lugares que ya existían conservan el precio que haya corregido el administrador.
	 *
	 * @return resumen con el número de lugares agregados y actualizados
	 * @throws DatosInvalidosException  si ninguna categoría tiene etiquetas o ninguna zona tiene
	 *                                  latitudes
	 * @throws ServicioExternoException si Overpass no responde
	 */
	public String cargarLugares() {
		List<Categoria> categorias = categoriasConEtiquetas();
		List<Zona> zonas = zonasConLatitudes();
		if (categorias.isEmpty() || zonas.isEmpty()) {
			throw new DatosInvalidosException(
					"Para cargar lugares, al menos una categoría debe tener etiqueta de OpenStreetMap y precio de "
							+ "referencia, y al menos una zona debe tener sus latitudes");
		}
		OverpassRespuestaDTO respuesta = ExternalHTTPRequestHandler
				.doPostOverpassAndConvertToDto(armarConsulta(categorias, zonas));
		if (respuesta == null || respuesta.getElements() == null) {
			throw new ServicioExternoException("OpenStreetMap no respondió, intenta más tarde");
		}
		int nuevos = 0;
		int actualizados = 0;
		for (OverpassElementoDTO e : respuesta.getElements()) {
			int resultado = guardar(e, categorias, zonas);
			if (resultado == 1) {
				nuevos++;
			} else if (resultado == 2) {
				actualizados++;
			}
		}
		return "Se agregaron " + nuevos + " lugares y se actualizaron " + actualizados;
	}

	/**
	 * Lista las categorías que tienen etiquetas de OpenStreetMap y precio de referencia.
	 *
	 * @return categorías que se pueden cargar
	 */
	private List<Categoria> categoriasConEtiquetas() {
		List<Categoria> categorias = new ArrayList<>();
		for (Categoria c : categoriaRepo.findAll()) {
			boolean conEtiquetas = c.getEtiquetaOsm() != null && !c.getEtiquetaOsm().isBlank();
			if (conEtiquetas && c.getPrecioReferencia() != null) {
				categorias.add(c);
			}
		}
		return categorias;
	}

	/**
	 * Lista las zonas que tienen sus latitudes.
	 *
	 * @return zonas que se pueden cargar
	 */
	private List<Zona> zonasConLatitudes() {
		List<Zona> zonas = new ArrayList<>();
		for (Zona z : zonaRepo.findAll()) {
			if (z.getLatitudMinima() != null && z.getLatitudMaxima() != null) {
				zonas.add(z);
			}
		}
		return zonas;
	}

	/**
	 * Ajusta el precio de referencia de una categoría al rango de gasto de una zona.
	 *
	 * @param precio precio de referencia de la categoría
	 * @param zona   zona del lugar
	 * @return el precio, o el límite de la zona que se haya pasado
	 */
	public static BigDecimal precioEnZona(BigDecimal precio, Zona zona) {
		if (zona.getGastoMinimo() != null && precio.compareTo(zona.getGastoMinimo()) < 0) {
			return zona.getGastoMinimo();
		}
		if (zona.getGastoMaximo() != null && precio.compareTo(zona.getGastoMaximo()) > 0) {
			return zona.getGastoMaximo();
		}
		return precio;
	}

	/**
	 * Arma la consulta de Overpass: todas las etiquetas de las categorías dentro de la franja que
	 * cubren las zonas.
	 *
	 * @param categorias categorías con etiquetas
	 * @param zonas      zonas con latitudes
	 * @return consulta en el lenguaje de Overpass
	 */
	String armarConsulta(List<Categoria> categorias, List<Zona> zonas) {
		double sur = zonas.get(0).getLatitudMinima();
		double norte = zonas.get(0).getLatitudMaxima();
		for (Zona z : zonas) {
			sur = Math.min(sur, z.getLatitudMinima());
			norte = Math.max(norte, z.getLatitudMaxima());
		}
		String area = String.format(Locale.ROOT, "(%.4f,%.4f,%.4f,%.4f)", sur, UbicacionValidator.LONGITUD_MINIMA,
				norte, UbicacionValidator.LONGITUD_MAXIMA);
		StringBuilder sb = new StringBuilder("[out:json][timeout:60];(");
		for (Categoria c : categorias) {
			for (String etiqueta : c.getEtiquetaOsm().split(",")) {
				String[] partes = etiqueta.trim().split("=");
				sb.append("nwr[\"").append(partes[0]).append("\"=\"").append(partes[1]).append("\"][\"name\"]")
						.append(area).append(";");
			}
		}
		return sb.append(");out center tags;").toString();
	}

	/**
	 * Agrega o actualiza el lugar de un elemento de OpenStreetMap.
	 *
	 * @param e          elemento de Overpass
	 * @param categorias categorías con etiquetas
	 * @param zonas      zonas con latitudes
	 * @return 1 si se agregó, 2 si se actualizó y 0 si se descartó
	 */
	private int guardar(OverpassElementoDTO e, List<Categoria> categorias, List<Zona> zonas) {
		Map<String, String> tags = e.getTags();
		if (!tieneNombre(tags)) {
			return 0;
		}
		double[] punto = coordenadas(e);
		if (punto.length == 0) {
			return 0;
		}
		double lat = punto[0];
		double lon = punto[1];
		Categoria categoria = buscarCategoria(tags, categorias);
		Zona zona = buscarZona(lat, zonas);
		if (categoria == null || zona == null) {
			return 0;
		}
		String osmId = e.getType() + "/" + e.getId();
		Lugar l = lugarRepo.findByOsmId(osmId);
		int resultado = 2;
		if (l == null) {
			l = new Lugar();
			l.setOsmId(osmId);
			l.setPrecioEstimado(true);
			resultado = 1;
		}
		l.setNombre(recortar(tags.get("name").trim(), 255));
		l.setDireccion(recortar(direccion(tags), 255));
		l.setLatitud(lat);
		l.setLongitud(lon);
		l.setCategoria(categoria);
		l.setZona(zona);
		if (l.isPrecioEstimado()) {
			l.setCostoEstimado(precioEnZona(categoria.getPrecioReferencia(), zona));
		}
		lugarRepo.save(l);
		return resultado;
	}

	/**
	 * Indica si el elemento tiene nombre.
	 *
	 * @param tags etiquetas del elemento
	 * @return true si tiene la etiqueta name con texto
	 */
	private boolean tieneNombre(Map<String, String> tags) {
		return tags != null && tags.get("name") != null && !tags.get("name").isBlank();
	}

	/**
	 * Saca la latitud y la longitud del elemento, o de su centro si es un edificio o un área.
	 *
	 * @param e elemento de Overpass
	 * @return arreglo con latitud y longitud, o vacío si no tiene o quedan fuera del área
	 */
	private double[] coordenadas(OverpassElementoDTO e) {
		Double lat = e.getLat();
		Double lon = e.getLon();
		if ((lat == null || lon == null) && e.getCenter() != null) {
			lat = e.getCenter().getLat();
			lon = e.getCenter().getLon();
		}
		if (lat == null || lon == null) {
			return new double[0];
		}
		boolean latitudValida = lat >= UbicacionValidator.LATITUD_MINIMA && lat <= UbicacionValidator.LATITUD_MAXIMA;
		boolean longitudValida = lon >= UbicacionValidator.LONGITUD_MINIMA && lon <= UbicacionValidator.LONGITUD_MAXIMA;
		if (!latitudValida || !longitudValida) {
			return new double[0];
		}
		return new double[] { lat, lon };
	}

	/**
	 * Busca la categoría a la que pertenece un elemento según sus etiquetas.
	 *
	 * @param tags       etiquetas del elemento
	 * @param categorias categorías con etiquetas
	 * @return la primera categoría que coincide, o null
	 */
	private Categoria buscarCategoria(Map<String, String> tags, List<Categoria> categorias) {
		for (Categoria c : categorias) {
			for (String etiqueta : c.getEtiquetaOsm().split(",")) {
				String[] partes = etiqueta.trim().split("=");
				if (partes[1].equals(tags.get(partes[0]))) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * Busca la zona en cuya franja de latitudes queda un punto.
	 *
	 * @param lat   latitud del punto
	 * @param zonas zonas con latitudes
	 * @return la zona, o null si ninguna lo contiene
	 */
	private Zona buscarZona(double lat, List<Zona> zonas) {
		for (Zona z : zonas) {
			if (lat >= z.getLatitudMinima() && lat < z.getLatitudMaxima()) {
				return z;
			}
		}
		return null;
	}

	/**
	 * Arma la dirección con las etiquetas addr:street y addr:housenumber.
	 *
	 * @param tags etiquetas del elemento
	 * @return dirección, o un texto que indica que no está registrada
	 */
	private String direccion(Map<String, String> tags) {
		String calle = tags.get("addr:street");
		if (calle == null || calle.isBlank()) {
			return SIN_DIRECCION;
		}
		String numero = tags.get("addr:housenumber");
		return numero == null || numero.isBlank() ? calle.trim() : calle.trim() + " # " + numero.trim();
	}

	/**
	 * Recorta un texto al largo de su columna.
	 *
	 * @param s   texto
	 * @param max largo máximo
	 * @return el texto recortado
	 */
	private String recortar(String s, int max) {
		return s.length() > max ? s.substring(0, max) : s;
	}
}
