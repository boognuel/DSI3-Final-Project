package co.edu.unbosque;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Prueba de arranque de la aplicación completa. Se conecta a la base de datos configurada.
 */
@SpringBootTest
class BogoparcheApplicationTests {

	@Autowired
	private ApplicationContext context;

	/**
	 * Comprueba que el contexto de Spring arranque sin errores.
	 */
	@Test
	void contextLoads() {
		assertNotNull(context);
	}

}
