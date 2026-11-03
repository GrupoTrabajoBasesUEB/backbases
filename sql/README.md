# Scripts de la base de datos Proyecto_db

Scripts para crear desde cero la base de datos de la cadena de ferreterías, sembrarla con datos de prueba y ejecutar las consultas del documento de álgebra relacional.

## Requisitos

- MySQL 8.0.31 o superior (el proyecto usa MySQL Community Server 8.4 LTS). INTERSECT y EXCEPT, usados en `03_consultas.sql`, no existen en versiones anteriores.
- Un usuario con permiso para crear bases de datos, tablas y vistas.

## Orden de ejecución

| Orden | Script | Qué hace |
|---|---|---|
| 1 | `01_creacion_tablas.sql` | Crea la base `Proyecto_db` (utf8mb4), borra las tablas y vistas si ya existían, y crea las 22 tablas con sus llaves primarias, foráneas, UNIQUE, CHECK e índices, más las vistas `V_PRODUCTO_STOCK` y `V_VENTA_TOTAL`. |
| 2 | `02_siembra_datos.sql` | Inserta los datos de prueba en las 22 tablas. |
| 3 | `03_consultas.sql` | Ejecuta las 60 consultas del documento de álgebra relacional. Solo lee datos. |

`01_creacion_tablas.sql` borra las tablas antes de crearlas: ejecutarlo sobre una base con datos los elimina. Para volver al estado inicial basta con ejecutar otra vez el 1 y luego el 2.

### Con el cliente de MySQL

```bash
mysql -u USUARIO -p < 01_creacion_tablas.sql
mysql -u USUARIO -p < 02_siembra_datos.sql
mysql -u USUARIO -p < 03_consultas.sql
```

### Con phpMyAdmin

Abrir la pestaña SQL, pegar el contenido de cada script y ejecutarlo, en el mismo orden. La siembra inserta las tablas en un orden que respeta las llaves foráneas, así que funciona con la revisión de llaves foráneas activada.

## Qué queda en la base

| Tabla | Filas |
|---|---|
| MARCA | 10 |
| CATEGORIA | 14 (6 principales y 8 subcategorías) |
| PRODUCTO | 19 |
| METODO_PAGO | 10 |
| SUCURSAL | 10, cada una con su administrador encargado |
| EMPLEADO | 23 (11 administradores y 12 vendedores), todos con al menos un teléfono |
| CLIENTE | 14 (10 naturales, incluido el consumidor final, y 4 jurídicos) |
| INVENTARIO | 173 |
| PROVEEDOR | 10 |
| SUMINISTRO | 142 |
| EVALUACION | 30 |
| VENTA | 120, con 241 líneas en DETALLE_VENTA |

Las fechas de las ventas y evaluaciones se calculan desde el día en que se ejecuta la siembra (`CURDATE()`), así que los datos siempre se ven recientes.

## Usuarios de prueba

| Correo | Rol | Sede | Clave de seguridad |
|---|---|---|---|
| laura.gomez@ferreteria.demo | Administradora, nivel 3 | Encargada de Sede Centro | demo-admin |
| andres.pardo@ferreteria.demo | Administrador, nivel 2 | Encargado de Sede Norte | demo-compras |
| sofia.restrepo@ferreteria.demo | Administradora, nivel 2 | Encargada de Sede Laureles | demo-sede |
| ricardo.moreno@ferreteria.demo | Administrador, nivel 1 | Encargado de Sede Kennedy | demo-kennedy |
| paula.arango@ferreteria.demo | Administradora, nivel 1 | Encargada de Sede Envigado | demo-envigado |
| hernando.valencia@ferreteria.demo | Administrador, nivel 2 | Encargado de Sede Chipichape | demo-chipichape |
| marcela.pertuz@ferreteria.demo | Administradora, nivel 1 | Encargada de Sede Alto Prado | demo-altoprado |
| oscar.rueda@ferreteria.demo | Administrador, nivel 2 | Encargado de Sede Cabecera | demo-cabecera |
| luisa.cardona@ferreteria.demo | Administradora, nivel 1 | Encargada de Sede Circunvalar | demo-circunvalar |
| javier.julio@ferreteria.demo | Administrador, nivel 2 | Encargado de Sede Bocagrande | demo-bocagrande |
| camila.rojas@ferreteria.demo | Vendedora | Sede Centro |  |
| julian.herrera@ferreteria.demo | Vendedor | Sede Norte |  |
| daniela.ortiz@ferreteria.demo | Vendedora | Sede Norte |  |
| mateo.castano@ferreteria.demo | Vendedor | Sede Laureles |  |
| andrea.salazar@ferreteria.demo | Vendedora | Sede Kennedy |  |
| sebastian.ospina@ferreteria.demo | Vendedor | Sede Envigado |  |
| valeria.cuellar@ferreteria.demo | Vendedora | Sede Chipichape |  |
| kevin.barrios@ferreteria.demo | Vendedor | Sede Alto Prado |  |
| diana.quintero@ferreteria.demo | Vendedora | Sede Cabecera |  |
| felipe.zuluaga@ferreteria.demo | Vendedor | Sede Circunvalar |  |
| yuliana.marrugo@ferreteria.demo | Vendedora | Sede Bocagrande |  |
| sergio.che2107@gmail.com | Administrador, nivel 3 | Sin sede a cargo | La que eligió el equipo |
| serbuxcuenta@gmail.com | Vendedor | Sede Centro | |

Los usuarios `@ferreteria.demo` tienen la contraseña de demostración `Ferreteria2026`. Los dos últimos tienen la contraseña que eligió el equipo. Contraseñas y claves de seguridad se guardan como hash bcrypt. Solo los administradores tienen clave de seguridad: es el segundo factor que se pide al eliminar un registro.
