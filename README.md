# Control de vehículos

Sistema de gestión vehicular (Spring Boot 4, Java 25, MySQL 8.4, Thymeleaf). Estado actual:

- **Vehículos**: registro, edición y baja lógica.
- **Configuración**: localidades, tipos de uso, rutinas de mantenimiento (10K/20K/30K) y parámetros de mantenimiento.
- **Mantenimiento**: historial por vehículo y programación automática del siguiente (próximo km y fecha de referencia).
- **Kilometraje**: captura semanal de lecturas del odómetro con recálculo en vivo, historial con diferencias y
  seguimiento de cuántos km faltan para el siguiente mantenimiento.
- **Alertas**: panel de mantenimientos vencidos y próximos (por km o por fecha) y avisos informativos, con un contador
  en la barra de navegación; los umbrales son configurables.
- **Reportes**: vehículos pendientes, mantenimientos realizados y próximos vencimientos, con filtros, tablas que se
  pueden ordenar y buscar, y descarga en Excel (.xlsx) o CSV.
- **Usuarios y roles**: cuentas en base de datos con tres roles (Administrador, Operador y Consulta), cambio y
  restablecimiento de contraseña, bloqueo por intentos fallidos y último acceso.

## Arquitectura

Hexagonal por módulo (`vehiculo`, `configuracion`, `mantenimiento`, `kilometraje`, `alertas`, `reportes`, `usuarios`) más
un kernel mínimo (`shared`):

```
<modulo>/
├─ domain/                         modelo y reglas de negocio (Java puro: sin Spring, JPA ni Lombok)
├─ application/
│  ├─ port/in                      casos de uso (lo que ofrece el módulo)
│  ├─ port/out                     repositorios (lo que necesita el módulo)
│  └─ service                      implementación de los casos de uso
└─ infrastructure/adapter/
   ├─ in/web                       controladores y formularios Thymeleaf
   └─ out/persistence              entidades JPA, repositorios Spring Data, mappers, adaptadores
```

Las dependencias entre módulos van en un solo sentido y solo por puertos de entrada:
`reportes → alertas → kilometraje → mantenimiento → vehiculo → configuracion` (y `kilometraje`/`alertas` leen sus
umbrales de `configuracion`; `reportes` también consulta directamente `mantenimiento`, `vehiculo` y `configuracion`).
Nadie depende de `reportes`. El módulo `usuarios` es independiente: no depende de ningún otro módulo y ninguno depende de
él (los permisos se aplican por nombre de rol en `SecurityConfig`). Está verificado con ArchUnit
(`ArquitecturaHexagonalTest`).

## Cómo ejecutar

Requisitos: JDK 25 (o el que defina `java.version` en `pom.xml`) y Docker en ejecución.

```powershell
./mvnw spring-boot:run
```

Spring Boot levanta MySQL solo con `compose.yaml` (puerto de host dinámico) y Flyway aplica las migraciones
(`src/main/resources/db/migration`): esquema, tipos de uso OPERATIVO/RELEVO, rutinas 10K/20K/30K, parámetros de
mantenimiento (10,000 km y 6 meses), historial de mantenimientos, lecturas de kilometraje y umbrales de alerta
(1,000 km, 30 días y 7 días) y la tabla de usuarios. Abre <http://localhost:8080>.

**Administrador inicial**: con la tabla de usuarios vacía (primer arranque) la aplicación crea un administrador con
`app.security.usuario` y `app.security.password`. En el perfil `dev` (activo por defecto) son `admin` / `admin123`
(`src/main/resources/application-dev.yaml`). Fuera de `dev` hay que definir las variables de entorno `APP_SECURITY_USUARIO`
y `APP_SECURITY_PASSWORD` en ese primer arranque (si faltan y no hay usuarios, la aplicación no arranca). Después esas
propiedades ya no se usan: los usuarios se administran en **Configuración → Usuarios** (cambia la contraseña inicial).

Primeros pasos en la aplicación: crea al menos una **localidad** (no hay semilla de localidades), registra
vehículos, registra sus mantenimientos desde la lista de vehículos y captura el kilometraje en **Kilometraje**.

> Las migraciones se aplican solo con Flyway al arrancar la aplicación. No ejecutes los `.sql` a mano sobre tu base de
> desarrollo: Flyway no los registraría y el siguiente arranque fallaría.

## Usuarios y roles

| Rol | Puede |
|---|---|
| **Administrador** | Todo, incluidos **Usuarios** y **Configuración** (localidades, tipos de uso, rutinas, parámetros y umbrales). |
| **Operador** | Registrar y editar vehículos, mantenimientos y lecturas; ver alertas y reportes. Sin configuración ni usuarios. |
| **Consulta** | Solo lectura: lista de vehículos, programación de mantenimiento, alertas y reportes (con descargas). |

- Los permisos se aplican por URL en `SecurityConfig` (acceso denegado → página "No tienes permiso"); la interfaz además
  oculta lo que el rol no puede usar. Los nombres de rol de `SecurityConfig` coinciden con el enum `Rol`.
- El nombre de usuario va en minúsculas (3–30 caracteres: letras, dígitos, punto, guion o guion bajo) y es único. Las
  contraseñas tienen de 8 a 72 caracteres y no pueden ser iguales al nombre de usuario; se guardan con BCrypt.
- Un administrador **no puede desactivarse ni quitarse el rol a sí mismo**, y **siempre debe quedar al menos un
  administrador activo**. Desactivar a un usuario, cambiarle el rol o restablecerle la contraseña **cierra sus sesiones
  abiertas** (su siguiente petición lo lleva al inicio de sesión).
- **Restablecer contraseña** (administrador, a otro usuario) y **Cambiar mi contraseña** (cada usuario, pidiendo la actual;
  menú del usuario en la barra). No hay cambio obligatorio en el primer ingreso.
- **Bloqueo**: tras `app.security.max-intentos-fallidos` (5) contraseñas incorrectas seguidas la cuenta se bloquea
  `app.security.minutos-bloqueo` (15) minutos; se libera sola o con **Desbloquear** (administrador). Un acceso correcto
  reinicia el conteo. La lista de usuarios muestra el estado (activo, inactivo, bloqueado) y el **último acceso**.

## Apariencia

La interfaz usa la misma base visual que la aplicación `pharmacy` (Thymeleaf + Bootstrap):

- **Bootstrap 5.3.3** y **Bootstrap Icons 1.11.3** se cargan desde `cdn.jsdelivr.net` (hace falta internet al abrir la
  aplicación; el script de Bootstrap va al final de cada página con el fragmento `layout :: scripts`).
- `static/css/tema.css`: tema verde (`#198754`) con la barra de navegación de color, tablas sin salto de línea y reglas de
  impresión. Es la versión fija de lo que `pharmacy` genera desde su configuración de apariencia (que aquí no existe).
- `static/css/app.css`: solo lo propio de este sistema (tarjetas de resumen de alertas, captura semanal, ordenar y buscar en
  tablas) y los ganchos que usan `static/js/seguimiento-semanal.js` y `static/js/tablas-interactivas.js`.
- La navegación (`templates/fragments/layout.html`) agrupa las pantallas de configuración en un menú desplegable.

## Pruebas

```powershell
./mvnw test
```

- Sin Docker: dominio, servicios (repositorios en memoria), controladores (`@WebMvcTest` con plantillas reales),
  seguridad y ArchUnit.
- Con Docker activo (Testcontainers + MySQL): `CatalogosPersistenceTest`, `VehiculoPersistenceAdapterTest`,
  `MantenimientoPersistenceAdapterTest`, `LecturaKilometrajePersistenceAdapterTest`, `UsuarioPersistenceAdapterTest` y
  `ControlVehiculosApplicationTests`.

## Reglas principales

**Vehículos**
- Placa (4–10 caracteres: letras, dígitos y guion) y código de equipo (hasta 30, sin espacios) son únicos y se
  normalizan a mayúsculas.
- Desactivar un vehículo es una baja lógica; un vehículo inactivo no se edita ni recibe mantenimientos ni lecturas
  hasta reactivarlo.
- Al registrar o cambiar localidad, tipo de uso o rutina, el valor elegido debe existir y estar activo.
- Edición con bloqueo optimista: si otro usuario modificó el vehículo, se pide recargar.

**Mantenimiento**
- Cada mantenimiento registra fecha, kilometraje y rutina realizada (10K, 20K o 30K). El historial es de solo
  agregar: no se editan ni se borran registros.
- La fecha no puede ser futura y el odómetro solo crece: no se acepta un kilometraje menor que el de un
  mantenimiento anterior (o de la misma fecha) ni mayor que el de uno posterior.
- **Próximo mantenimiento** = kilometraje registrado + intervalo de km, y fecha de referencia = fecha + intervalo de
  meses (hoy 10,000 km y 6 meses, sin importar la rutina realizada). Los **días restantes** se cuentan hasta esa
  fecha; negativo significa vencido. Se calcula al consultar con los parámetros vigentes.
- El "último mantenimiento" de un vehículo es el de mayor fecha (desempate por orden de registro). Al registrar uno que
  sea el más reciente, se actualiza la última rutina realizada de la ficha del vehículo.
- Los intervalos se cambian en **Parámetros** (menú de configuración); aplican a toda la flota.

**Kilometraje**
- Hay a lo sumo **una lectura por vehículo y fecha**. Volver a ingresar la lectura de la misma fecha la **reemplaza**
  (así se corrige un error de captura; reenviar la pantalla semanal es seguro). Además se puede **eliminar la última
  lectura** de un vehículo; no hay otro borrado.
- La fecha no puede ser futura y el odómetro solo crece (respecto a las demás lecturas), y una lectura de igual o
  posterior fecha que el último mantenimiento no puede marcar menos km que él.
- **Seguimiento**: `km actual` = el mayor entre la última lectura y el km del último mantenimiento;
  `km restantes` = próximo km − km actual (negativo = ya se pasó). Ejemplo: mantenimiento a 100,000 km, próximo a
  110,000 km, lectura 107,500 km → faltan 2,500 km. Un vehículo queda **pendiente de lectura** si no tiene lecturas o la
  última tiene más días que el umbral configurado (7 por defecto).
- La pantalla **Kilometraje → Seguimiento semanal** lista todos los vehículos activos (pendientes primero) con una
  casilla de lectura nueva por fila. Mientras se escribe se recalculan los km recorridos y los km restantes y se avisa si
  el valor es menor que el último conocido o si el salto desde la última lectura supera los 5,000 km (constante
  `SALTO_SOSPECHOSO_KM` en `static/js/seguimiento-semanal.js`). Enter pasa a la fila siguiente. Se guardan las filas
  válidas aunque otras fallen; las que fallan conservan lo digitado y muestran el motivo.
- Cada vehículo tiene además su historial con km recorridos y días desde la lectura anterior.

**Alertas** (`/alertas`; el número de alertas vencidas + próximas aparece en la barra de navegación)
- Se calculan al consultar a partir del seguimiento de cada vehículo activo; no se guardan ni se posponen: desaparecen
  cuando se registra el mantenimiento o la lectura que las resuelve.
- **Vencido**: `km restantes < 0` o `días restantes < 0` (se indica si es por km, por fecha o ambos).
- **Próximo** (si no está vencido): faltan `umbral de km` km o menos, o `umbral de días` días o menos; el límite exacto
  cuenta como próximo.
- **Informativas** (no cuentan en la insignia): *sin mantenimientos registrados* (no se puede programar) y *lectura
  pendiente*. Esta última puede acompañar a cualquier otra alerta del mismo vehículo.
- Los umbrales se editan en **Parámetros → Umbrales de alerta** (`/configuracion/alertas`): 1,000 km, 30 días y 7 días de
  lectura pendiente por defecto. El de lectura pendiente también gobierna el "pendiente" del seguimiento semanal.
- La insignia nunca rompe una página: se calcula solo en peticiones GET y, si falla, se omite y se registra en el log.

**Reportes** (`/reportes`; solo lectura, se calculan al consultar con los datos del momento; sin migraciones propias)
- **Vehículos pendientes**: vehículos activos con mantenimiento **vencido o próximo** (misma clasificación y umbrales que las
  alertas; no incluye "lectura pendiente"), vencidos primero, con motivo, último mantenimiento, próximo km, km actual, km y
  días restantes y fecha de referencia. Filtros: estado (todos/vencidos/próximos), localidad y tipo de uso.
- **Mantenimientos realizados**: del período (por defecto los últimos 90 días hasta hoy; la fecha final no puede ser futura),
  del más reciente al más antiguo, incluyendo vehículos hoy inactivos. Filtros: fechas, localidad, rutina y placa (contiene,
  sin distinguir mayúsculas). Muestra el total y el desglose por rutina y por localidad.
- **Próximos vencimientos**: vehículos activos con programación a quienes les faltan **N días o menos, o M km o menos**
  (por defecto 60 días / 2,000 km), ordenados por días restantes; se pueden incluir u omitir los ya vencidos y filtrar por
  localidad. Los vehículos sin mantenimientos no tienen vencimiento y no aparecen.
- **Descarga en Excel (.xlsx)** de cada reporte con los mismos filtros (`/reportes/<reporte>/xlsx`): hoja de datos con
  encabezado en negrita, fila congelada, autofiltro, fechas y números **reales** (se pueden ordenar, filtrar y sumar) y una hoja
  "Resumen" con la fecha de emisión, los filtros aplicados (con nombres) y los totales. Los textos se guardan como texto, nunca
  como fórmulas. Se genera con Apache POI (`poi.version` en el `pom.xml`), usado solo en la capa de infraestructura.
- **Descarga CSV** (`/reportes/<reporte>/csv`): UTF-8 con BOM (acentos correctos en Excel), fechas ISO y números sin separadores
  de miles. El separador es `app.reportes.separador-csv` (por defecto `;`, el de Excel en configuración regional hispana; si el
  archivo se abre en una sola columna, cámbialo a `,` en `application.yaml`). Los textos que empiezan con `=`, `+`, `-` o `@` se
  prefijan con `'` para evitar la inyección de fórmulas. El CSV y el Excel salen de la misma tabla (mismas columnas).
- **Tablas interactivas** (`static/js/tablas-interactivas.js`): clic en un encabezado para ordenar (por valor real: `110,000 km`
  ordena como número), buscador dentro de la tabla (sin distinguir mayúsculas ni acentos) y contador "Mostrando X de Y". Sin
  JavaScript las tablas siguen funcionando, solo sin ordenar ni buscar.
- **Límites**: el período de "realizados" puede ser de hasta 5 años (1,827 días) y los horizontes de vencimientos, de hasta 3,650
  días y 1,000,000 km.

## Pendiente

Impresión/PDF de los reportes, reportes programados o enviados por correo, el mismo ordenar/buscar en las demás tablas, otros canales de aviso (correo, WhatsApp), posponer o
marcar alertas como atendidas, paginación y búsqueda en listados, cambio de contraseña obligatorio en el primer ingreso,
recuperación de contraseña por correo, doble factor y roles editables.
