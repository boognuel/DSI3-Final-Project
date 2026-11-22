# Bogoparche

Aplicación web que recomienda planes en Bogotá, de la calle 75 a la calle 222, según cuánto dinero tiene cada persona. Los usuarios buscan planes por presupuesto y categoría, arman salidas con varios lugares propuestos, invitan a sus amigos por correo y piden la ruta para llegar.

## Estructura

- `back`: API REST en Java 21 con Spring Boot 4.1.2, MySQL, JWT, Spring Mail y Apache POI.
- `front`: aplicación en Angular 22 con módulos, Bootstrap, Font Awesome, animate.css, Leaflet y Chart.js.

## Back

### Configuración

El archivo `back/src/main/resources/application.properties` no se sube al repositorio. Cada integrante crea el suyo con estas claves:

| Clave | Para qué |
| --- | --- |
| `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` | Conexión a MySQL. |
| `spring.jpa.hibernate.ddl-auto=update` | JPA crea y actualiza las tablas. |
| `jwt.secret` | Llave con la que se firman los tokens. Mínimo 32 caracteres. |
| `app.frontend-url`, `app.cors.allowed-origin` | Dirección del front, para los enlaces de los correos y CORS. |
| `app.admin.email`, `app.admin.password` | Administrador que se crea al iniciar si no existe. |
| `spring.mail.*` | Servidor de correo para la verificación, la recuperación de la contraseña y las invitaciones. |
| `groq.model`, `groq.api.key` | Modelo y llave de Groq, que redacta las indicaciones de las rutas. |

### Ejecución

```bash
cd back
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080` y Swagger en la raíz de esa dirección.

### APIs externas

Se consumen desde el back con `ExternalHTTPRequestHandler`:

- **Overpass (OpenStreetMap):** carga los lugares de cada categoría dentro de las zonas. La ejecuta el administrador.
- **OSRM:** calcula el camino a un lugar a pie, en bicicleta o en carro.
- **Groq:** redacta las indicaciones del recorrido. Si no responde, no se entrega la ruta.

OpenStreetMap no tiene precios. Por eso cada categoría tiene un precio de referencia por persona, que toman sus lugares, ajustado al rango de gasto de su zona, hasta que el administrador corrige el de un lugar.

## Front

```bash
cd front
npm install
npm start
```

La aplicación queda en `http://localhost:4200`. La dirección del back está en `src/environments`. La ubicación del navegador es obligatoria y la app avisa si el usuario está fuera de la zona que cubre.

## Ramas

Cada módulo tiene su rama en el back (`back-*`) y en el front (`front-*`). Las ramas del back se integran en `backend` y las del front en `frontend`, y desde ahí pasan a `main`.
