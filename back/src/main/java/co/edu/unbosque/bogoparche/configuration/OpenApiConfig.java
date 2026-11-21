package co.edu.unbosque.bogoparche.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para documentar la API REST de BogoParche. Define la
 * información general de la API, el esquema de seguridad JWT y respuestas reutilizables.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Define componentes reutilizables para la documentación OpenAPI. Incluye ejemplos de respuestas
   * comunes para mejorar la documentación.
   *
   * @return Configuración personalizada de OpenAPI
   */
  @Bean
  public OpenAPI customOpenAPI() {
    // Crear la descripción principal con HTML correctamente formateado
    String mainDescription =
        "<h2>API REST de BogoParche</h2><p>BogoParche recomienda planes para salir con amigos en"
            + " Bogotá, de la calle 75 hacia el norte, según el presupuesto y los gustos de cada"
            + " usuario. Esta API gestiona usuarios, salidas, lugares, invitaciones por correo,"
            + " rutas y la auditoría del sistema.</p><h3>Conceptos básicos:</h3><ul>   "
            + " <li><strong>JWT (JSON Web Token)</strong>: al iniciar sesión se recibe un token que"
            + " debe enviarse en las cabeceras de las peticiones siguientes.</li>   "
            + " <li><strong>Autenticación</strong>: verificación de la identidad con usuario y"
            + " contraseña.</li>    <li><strong>Autorización</strong>: define qué acciones puede"
            + " realizar un usuario autenticado según su rol.</li></ul><h3>Flujo básico de"
            + " uso:</h3><ol>    <li>Registrarse con <code>/auth/register</code></li>   "
            + " <li>Verificar el correo con el enlace recibido</li>    <li>Iniciar sesión con"
            + " <code>/auth/login</code> para obtener el token JWT</li>    <li>Incluir el token en"
            + " el encabezado de las peticiones: <code>Authorization: Bearer"
            + " tu_token_jwt</code></li></ol><h3>Roles de usuario:</h3><ul>   "
            + " <li><strong>USER</strong>: crea salidas, elige lugares e invita amigos</li>   "
            + " <li><strong>ADMIN</strong>: gestiona usuarios, consulta la auditoría y descarga"
            + " el reporte</li></ul><h3>Códigos de estado HTTP comunes:</h3><ul>   "
            + " <li><strong>200/201</strong>: Operación exitosa</li>    <li><strong>400</strong>:"
            + " Error en la solicitud (datos incorrectos)</li>    <li><strong>401</strong>: No"
            + " autenticado (token inválido o expirado)</li>    <li><strong>403</strong>: No"
            + " autorizado (no tienes permisos suficientes)</li>    <li><strong>404</strong>:"
            + " Recurso no encontrado</li>    <li><strong>409</strong>: Conflicto (por ejemplo,"
            + " nombre de usuario ya existente)</li></ul>";

    // Crear la descripción del esquema de seguridad con HTML correctamente formateado
    String securityDescription =
        "Autenticación mediante JWT (JSON Web Token)."
            + "<p>Para autenticarte, sigue estos pasos:</p>"
            + "<ol>"
            + "    <li>Obtén un token JWT usando el endpoint <code>/auth/login</code></li>"
            + "    <li>Copia el token recibido en la respuesta</li>"
            + "    <li>Haz clic en el botón \"Authorize\" en la parte superior de esta página</li>"
            + "    <li>En el campo \"Value\", escribe: <code>Bearer tu_token_jwt</code></li>"
            + "    <li>Haz clic en \"Authorize\" y luego en \"Close\"</li>"
            + "</ol>"
            + "<p>Ahora podrás acceder a los endpoints protegidos.</p>";

    // Crear objeto Info con la descripción HTML
    io.swagger.v3.oas.models.info.Info info =
        new io.swagger.v3.oas.models.info.Info()
            .title("API de BogoParche")
            .version("1.0")
            .description(mainDescription)
            .contact(
                new io.swagger.v3.oas.models.info.Contact()
                    .name("Equipo BogoParche")
                    .url("https://github.com/boognuel/DSI3-Final-Project"))
            .license(
                new io.swagger.v3.oas.models.info.License()
                    .name("GNU GPL v3.0")
                    .url("https://www.gnu.org/licenses/gpl-3.0.html"));

    // Crear el esquema de seguridad
    io.swagger.v3.oas.models.security.SecurityScheme securityScheme =
        new io.swagger.v3.oas.models.security.SecurityScheme()
            .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description(securityDescription);

    return new OpenAPI()
        .info(info)
        .components(
            new Components()
                .addSecuritySchemes("bearerAuth", securityScheme)
                .addResponses(
                    "UnauthorizedError",
                    new ApiResponse()
                        .description("No autenticado - Token JWT inválido o expirado")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .addExamples(
                                            "error",
                                            new Example()
                                                .value(
                                                    "{\"error\": \"No autorizado\", \"mensaje\":"
                                                        + " \"Token inválido o expirado\"}")))))
                .addResponses(
                    "ForbiddenError",
                    new ApiResponse()
                        .description("Acceso prohibido - No tienes permisos suficientes")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .addExamples(
                                            "error",
                                            new Example()
                                                .value(
                                                    "{\"error\": \"Acceso prohibido\", \"mensaje\":"
                                                        + " \"No tienes permisos para esta"
                                                        + " operación\"}")))))
                .addResponses(
                    "NotFoundError",
                    new ApiResponse()
                        .description("Recurso no encontrado")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .addExamples(
                                            "error",
                                            new Example()
                                                .value(
                                                    "{\"error\": \"No encontrado\", \"mensaje\":"
                                                        + " \"El recurso solicitado no"
                                                        + " existe\"}"))))));
  }
}
