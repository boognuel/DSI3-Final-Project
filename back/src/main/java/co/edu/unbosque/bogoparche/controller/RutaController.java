package co.edu.unbosque.bogoparche.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.bogoparche.dto.RutaDTO;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.service.RutaService;
import jakarta.validation.Valid;

/**
 * Rutas para llegar a los lugares de una salida. Requiere sesión y participar en la salida.
 */
@RestController
@RequestMapping("/ruta")
public class RutaController {
	@Autowired
	private RutaService rutaService;

	/**
	 * Calcula la ruta desde la ubicación del usuario hasta un lugar propuesto en la salida.
	 *
	 * @param dto  salida, lugar, ubicación de origen y modo de transporte (CARRO, PIE o BICICLETA)
	 * @param auth usuario autenticado
	 * @return 201 con la distancia, la duración, la línea del camino en GeoJSON y las indicaciones
	 */
	@PostMapping
	public ResponseEntity<RutaDTO> calcular(@Valid @RequestBody RutaDTO dto, Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED).body(rutaService.calcular(dto, (User) auth.getPrincipal()));
	}
}
