package co.edu.unboque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Vive en el paquete raíz para que Spring escanee model, repository, service, controller, security, etc.
@SpringBootApplication
public class SalidasAmigosApplication {
	public static void main(String[] args) {
		SpringApplication.run(SalidasAmigosApplication.class, args);
	}
}
