# UEES | Diseño de Software | UCOM0310
## Semana 6 | Ae5 — Refactorización respaldada por pruebas unitarias

**Estudiante:** Justin Arreaga
**Caso:** sistema de reservas de tutorías (código heredado del Laboratorio 1).

El proyecto parte del diagnóstico del Laboratorio 1 (`docs/01…07`). En esta actividad se construyó
una red de pruebas JUnit 5 y, protegidos por ella, se aplicaron cuatro refactorizaciones con commits
incrementales. El comportamiento observable no cambió.

---

## Requisitos

- Java 21
- Maven 3.9+
- Git

## Ejecutar las pruebas

```bash
mvn clean test
```

Resultado esperado: `Tests run: 30, Failures: 0, Errors: 0` y `BUILD SUCCESS`.

## Ejecutar el programa

```bash
mvn compile exec:java -Dexec.mainClass="edu.uees.refactor.app.Main"
```

```text
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
Estado: CONFIRMADA
Total: 34.0
```

Línea base completa (seis escenarios y los escenarios extra):

```bash
mvn compile exec:java -Dexec.mainClass="edu.uees.refactor.app.LineaBaseManual"
```

---

## Estructura final

```text
src/main/java/edu/uees/refactor/
├── app/            Main, LineaBaseManual
├── domain/         Reserva, EstadoReserva, PeriodoReserva (record), TipoReserva (enum)
├── service/        ServicioReservas (orquesta), CalculadoraTarifa (precio)
└── infraestructura/ RepositorioReservas, NotificadorReservas (simulados con consola)

src/test/java/edu/uees/refactor/   30 pruebas JUnit 5 (patrón AAA)
docs/
├── 01…07  Diagnóstico del Laboratorio 1
├── 08_REPORTE_AE5.md   Reporte técnico de la Ae5
└── evidencias_ae5/     Salidas reales de mvn test, ejecución y regresiones
```

## Refactorizaciones aplicadas

| # | Técnica | Commit |
|---|---|---|
| 0 | Red de seguridad: 19 pruebas de caracterización | `test: caracterizar comportamiento actual de ServicioReservas con JUnit 5` |
| 1 | Decompose Conditional + constantes con nombre | `refactor: descomponer condicionales y nombrar reglas de ServicioReservas` |
| 2 | Extract Class (persistencia y notificación) | `refactor: extraer persistencia y notificacion de ServicioReservas` |
| 3 | Value Object `PeriodoReserva` (Data Clumps + Move Method) | `refactor: introducir PeriodoReserva para agrupar inicio y fin` |
| 4 | Extract Class `CalculadoraTarifa` + enum `TipoReserva` | `refactor: extraer politica de precios a CalculadoraTarifa y TipoReserva` |

Detalle, comparación antes/después y conclusiones en [`docs/08_REPORTE_AE5.md`](docs/08_REPORTE_AE5.md).
