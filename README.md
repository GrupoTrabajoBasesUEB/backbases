# backbases

API REST del sistema de información para una cadena de ferreterías, proyecto final de Bases de Datos 1 (Universidad El Bosque). La usa el front en Angular y trabaja sobre la base MySQL `Proyecto_db`.

- Spring Boot 4.1 con Java 21, empaquetado como WAR para Tomcat 11.
- Spring Data JPA sobre MySQL 8.4 LTS.
- Spring Security con tokens JWT y contraseñas con hash bcrypt.

## Requisitos

- JDK 21.
- Una base `Proyecto_db` creada y sembrada con los scripts de [`sql/`](sql/README.md). Al arrancar, Hibernate solo valida que las tablas coincidan con las entidades (`ddl-auto=validate`); no crea ni modifica tablas.

## Configuración

La configuración sale de variables de entorno. Ninguna credencial está en el código.

| Variable | Obligatoria | Qué es |
|---|---|---|
| `DB_URL` | Sí | URL JDBC de la base, por ejemplo `jdbc:mysql://HOST:3306/Proyecto_db?allowPublicKeyRetrieval=true&connectTimeout=20000` |
| `DB_USER` | Sí | Usuario de MySQL |
| `DB_PASSWORD` | Sí | Contraseña de ese usuario |
| `JWT_SECRET` | Sí | Clave para firmar los tokens, en Base64 y de al menos 32 bytes. Se puede generar con `openssl rand -base64 32` |
| `JWT_EXPIRATION_MS` | No | Duración de la sesión en milisegundos. Por defecto 28800000 (8 horas) |
| `CORS_ALLOWED_ORIGINS` | No | Orígenes del front que pueden llamar a la API, separados por comas. Por defecto `http://localhost:4200` |
| `PORT` | No | Puerto del servidor. Por defecto 8080 |

Las fechas y horas se manejan en la zona de Bogotá (`America/Bogota`).

## Ejecutar

```bash
./mvnw spring-boot:run
```

Para generar el WAR que se despliega en Tomcat:

```bash
./mvnw package
```

El archivo queda en `target/backbases-0.0.1-SNAPSHOT.war`.

## Pruebas

```bash
./mvnw test
```

Son 169 pruebas con JUnit:

- Las pruebas unitarias (servicios, controladores, seguridad y manejo de errores) no necesitan base y corren siempre.
- Las pruebas de integración (clases `*IntegracionTest`) solo corren si existe `application-local.properties` o la variable `DB_URL`. Se ejecutan contra esa base, con los usuarios de prueba de la siembra, y cada una se deshace al terminar o borra lo que creó.

## API

Todas las rutas empiezan por `/api` y responden JSON.

### Sesión

1. `POST /api/auth/login` con `{"correo": "...", "contrasena": "..."}` devuelve un token y los datos del empleado.
2. Las demás peticiones llevan el token en el encabezado `Authorization: Bearer <token>`.
3. `GET /api/auth/yo` devuelve el empleado de la sesión.

Toda eliminación de un administrador pide, además, su clave de seguridad en el encabezado `X-Clave-Seguridad`.

### Recursos

| Ruta | Contenido | Lectura | Cambios |
|---|---|---|---|
| `/api/ventas` | Ventas con sus líneas | Vendedor (su sede) y nivel 1 | Solo el vendedor registra |
| `/api/clientes` | Clientes naturales y jurídicos, con teléfonos y correos | Vendedor y nivel 1 | Vendedor y nivel 1; solo el administrador elimina |
| `/api/productos` | Productos con su stock total | Todos | Nivel 1 |
| `/api/inventario` | Existencias por sucursal y entradas de mercancía | Vendedor (su sede) y nivel 1 | Nivel 1 |
| `/api/proveedores` | Proveedores con teléfonos y correos | Nivel 2 | Nivel 2 |
| `/api/suministros` | Qué proveedor surte qué producto a qué sede, con precio y tiempo de entrega | Nivel 2 | Nivel 2 |
| `/api/evaluaciones` | Evaluaciones de calidad de cada suministro | Nivel 2 | Nivel 2 |
| `/api/empleados` | Administradores y vendedores | Todos | Nivel 3 |
| `/api/sucursales` | Sucursales y su administrador encargado | Todos | Nivel 3 |
| `/api/marcas`, `/api/categorias`, `/api/metodos-pago` | Catálogos | Todos | Nivel 3 |
| `/api/estadisticas` | Indicadores de ventas, inventario, sucursales y comisiones | Según el indicador; las comisiones, el vendedor la suya y el administrador desde el nivel 3 | No aplica |

"Todos" son los empleados con sesión: los formularios y las facturas necesitan esas listas, por ejemplo el selector de sede o el nombre del vendedor.


### Errores

Los errores responden con el código HTTP correspondiente (400, 401, 403, 404 o 409) y este cuerpo:

```json
{
  "estado": 409,
  "codigo": "CONFLICTO",
  "mensaje": "Texto para mostrar al usuario.",
  "campos": {},
  "fecha": "2026-11-05T10:15:30-05:00"
}
```

En los errores de validación, `campos` dice qué falló en cada campo.

## Estructura

```
src/main/java/co/edu/unbosque/backbases/
  controller/     rutas de la API
  service/        reglas del negocio y transacciones
  repository/     acceso a datos con Spring Data JPA y consultas nativas
  entity/         una entidad por tabla, con las llaves compuestas como @Embeddable
  dto/            objetos de entrada y salida de la API
  security/       JWT, usuarios, clave de seguridad
  configuration/  CORS y registro del interceptor de la clave
  exception/      excepciones del negocio y su traducción a respuestas HTTP
sql/              scripts de creación, siembra y consultas de la base
```

## Despliegue

Cada push a `main` dispara un flujo de GitHub Actions que compila el WAR y lo publica en Azure App Service con Tomcat 11. La API en producción está en `https://tomcat.serbux.qzz.io/api`.
