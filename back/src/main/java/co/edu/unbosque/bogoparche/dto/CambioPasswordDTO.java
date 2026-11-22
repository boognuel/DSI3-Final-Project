package co.edu.unbosque.bogoparche.dto;

import lombok.Data;

/**
 * Datos para que un usuario cambie su contraseña desde su perfil.
 */
@Data
public class CambioPasswordDTO {
	private String actual;
	private String nueva;
}
