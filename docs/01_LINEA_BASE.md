# Fase C | Línea base manual

Completa los seis escenarios **sin refactorizar el diseño**.

## Fase A | Entorno

| Herramienta | Versión | Observación |
|---|---|---|
| Java (PATH / `JAVA_HOME`) | Corretto 17.0.20 | Insuficiente: el `pom.xml` exige 21. |
| Java usado para compilar | OpenJDK 26.0.2 (`~/.jdks/openjdk-26.0.2`) | Compila con `source/target 21`. |
| Maven | 3.9.16 (incluido en IntelliJ IDEA 2026.2.3) | `mvn` no está en el PATH del sistema. |
| Git | 2.55.0.windows.3 | Operativo. |

### Errores de entorno encontrados (antes de corregir)

```text
$ mvn -version
bash: mvn: command not found

$ mvn clean compile      # con JAVA_HOME -> Corretto 17
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.15.0:compile
        (default-compile) on project semana6-lab-diagnostico:
        Fatal error compiling: error: invalid target release: 21
```

Son errores **de entorno**, no defectos del código. Se resolvieron sin tocar el proyecto: `JAVA_HOME` apuntando al JDK 26 y el Maven integrado de IntelliJ.

```text
$ mvn clean compile      # con JAVA_HOME -> OpenJDK 26.0.2
[WARNING] ... --release 21 is recommended instead of -source 21 -target 21 ...
[INFO] BUILD SUCCESS
```

La advertencia sale del `pom.xml` actual (usa `source`/`target` en lugar de `release`). No impide compilar y se deja sin cambios.

## Fase B | Ejecución de `Main` sin modificar

```bash
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.Main"
```

```text
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
Estado: CONFIRMADA
Total: 34.0
```

Coincide con la salida esperada del README.

### Registro de observaciones

| Observación | ¿Observable? | ¿Interno? | Comentario |
|---|---|---|---|
| La reserva termina CONFIRMADA | Sí | No | Contrato visible vía `getEstado()`. |
| Se imprime un mensaje de persistencia | Sí | No | Efecto externo en consola; hoy es el único rastro de "guardar". |
| Se imprime un mensaje de correo enviado | Sí | No | Efecto externo; ocurre antes de confirmar. |
| `ServicioReservas` contiene un `if` para VIP | No | Sí | Estructura; una refactorización puede cambiarlo. |
| El total VIP es 34.0 | Sí | No | Comportamiento a preservar (40 × 0.85). |
| `Reserva` almacena inicio y fin por separado | No | Sí | Detalle de datos; candidato a Data Clump. |
| `Main` usa `LocalDateTime.now()` | No | Sí | El resultado depende del reloj; no es reproducible entre días. |

## Arnés de evidencia

Para que la evidencia sea reproducible se creó `src/main/java/edu/uees/refactor/app/LineaBaseManual.java`. Usa una **fecha fija** (`LocalDateTime.of(2026, 10, 1, 10, 0)`) y periodos de 1 h. No modifica `ServicioReservas` ni `Reserva`.

```bash
mvn exec:java -Dexec.mainClass="edu.uees.refactor.app.LineaBaseManual"
```

## Escenarios de línea base

| ID | Escenario | Entrada principal | Estado | Retorno | Mensajes / excepción |
|---|---|---|---|---|---|
| LB-01 | NORMAL válida | NORMAL, correo válido, 5h | CONFIRMADA | 40.0 | "Guardando reserva R-001" + "Correo enviado a ana@uees.edu.ec" |
| LB-02 | VIP válida | VIP, correo válido, 5h | CONFIRMADA | 34.0 | "Guardando reserva R-002" + "Correo enviado a ana@uees.edu.ec" |
| LB-03 | Correo inválido | "incorrecto" | PENDIENTE | 0.0 | Ningún mensaje, sin excepción |
| LB-04 | Periodo inválido | fin <= inicio (fin = inicio) | PENDIENTE | 0.0 | Ningún mensaje, sin excepción |
| LB-05 | Límite válido | 2h anticipación | CONFIRMADA | 40.0 | "Guardando reserva R-005" + "Correo enviado a ana@uees.edu.ec" |
| LB-06 | Límite inválido | 1h anticipación | PENDIENTE | 0.0 | Ningún mensaje, sin excepción |

### Escenarios extra (revelan reglas implícitas)

| ID | Entrada | Estado | Retorno | Por qué importa |
|---|---|---|---|---|
| EX-01 | tipo `"vip"` (minúsculas) | CONFIRMADA | 40.0 | La comparación distingue mayúsculas: no aplica descuento. |
| EX-02 | tipo `null` | CONFIRMADA | 40.0 | Cualquier tipo desconocido se cobra como NORMAL. |
| EX-03 | correo `"@"` | CONFIRMADA | 40.0 | La validación de correo solo busca "@". |
| EX-04 | fin antes de inicio | PENDIENTE | 0.0 | Mismo resultado que fin = inicio. |
| EX-05 | correo `null` | PENDIENTE | 0.0 | Se rechaza sin excepción. |
| EX-06 | inicio en 2020, 5 h declaradas | CONFIRMADA | 40.0 | `horasAnticipacion` no se contrasta con `inicio`: se confirma una reserva en el pasado. |
| EX-07 | `procesar(null, 5)` | — | 0.0 | Sin excepción: el error se oculta en un 0. |

Estos casos se registran como **comportamiento actual**, no se corrigen. Decidir si son bugs es un cambio funcional y queda fuera del laboratorio.

### Salida real de consola

```text
=== Escenarios de línea base ===

[LB-01] NORMAL válida | tipo=NORMAL | correo=ana@uees.edu.ec | horas=5
Guardando reserva R-001
Correo enviado a ana@uees.edu.ec
  -> Estado: CONFIRMADA
  -> Retorno: 40.0

[LB-02] VIP válida | tipo=VIP | correo=ana@uees.edu.ec | horas=5
Guardando reserva R-002
Correo enviado a ana@uees.edu.ec
  -> Estado: CONFIRMADA
  -> Retorno: 34.0

[LB-03] Correo inválido | tipo=NORMAL | correo=incorrecto | horas=5
  -> Estado: PENDIENTE
  -> Retorno: 0.0

[LB-04] Periodo inválido (fin = inicio) | tipo=NORMAL | correo=ana@uees.edu.ec | horas=5
  -> Estado: PENDIENTE
  -> Retorno: 0.0

[LB-05] Límite válido (2 h) | tipo=NORMAL | correo=ana@uees.edu.ec | horas=2
Guardando reserva R-005
Correo enviado a ana@uees.edu.ec
  -> Estado: CONFIRMADA
  -> Retorno: 40.0

[LB-06] Límite inválido (1 h) | tipo=NORMAL | correo=ana@uees.edu.ec | horas=1
  -> Estado: PENDIENTE
  -> Retorno: 0.0

=== Escenarios extra (reglas implícitas) ===

[EX-01] Tipo "vip" en minúsculas | tipo=vip | correo=ana@uees.edu.ec | horas=5
Guardando reserva R-101
Correo enviado a ana@uees.edu.ec
  -> Estado: CONFIRMADA
  -> Retorno: 40.0

[EX-02] Tipo null | tipo=null | correo=ana@uees.edu.ec | horas=5
Guardando reserva R-102
Correo enviado a ana@uees.edu.ec
  -> Estado: CONFIRMADA
  -> Retorno: 40.0

[EX-03] Correo "@" | tipo=NORMAL | correo=@ | horas=5
Guardando reserva R-103
Correo enviado a @
  -> Estado: CONFIRMADA
  -> Retorno: 40.0

[EX-04] Fin antes de inicio | tipo=NORMAL | correo=ana@uees.edu.ec | horas=5
  -> Estado: PENDIENTE
  -> Retorno: 0.0

[EX-05] Correo null | tipo=NORMAL | correo=null | horas=5
  -> Estado: PENDIENTE
  -> Retorno: 0.0

[EX-06] Inicio en el pasado con 5 h declaradas | tipo=NORMAL | correo=ana@uees.edu.ec | horas=5
Guardando reserva R-106
Correo enviado a ana@uees.edu.ec
  -> Estado: CONFIRMADA
  -> Retorno: 40.0

[EX-07] procesar(null, 5)
  -> Retorno: 0.0
```

## Preguntas

1. **¿Qué valores cambian entre NORMAL y VIP?**
   Solo el retorno: 40.0 (LB-01) frente a 34.0 (LB-02), es decir 40 × 0.85. El estado final (CONFIRMADA) y los dos mensajes son idénticos. El descuento solo se aplica al texto exacto `"VIP"` (EX-01).
2. **¿Qué casos dejan la reserva en PENDIENTE?**
   LB-03 (correo sin "@"), LB-04 (fin = inicio) y LB-06 (1 h de anticipación). También EX-04 (fin antes de inicio) y EX-05 (correo null). En general, toda validación fallida.
3. **¿Qué devuelve `procesar()` cuando una entrada no es procesable?**
   Devuelve `0.0`. Es un valor mágico que representa el error, y es el mismo para los cuatro motivos de rechazo (null, correo, periodo, anticipación).
4. **¿Existe alguna excepción visible en el flujo actual?**
   No. Ningún escenario lanza excepción, ni siquiera `procesar(null, 5)` (EX-07) ni un correo `null` (EX-05).
5. **¿Qué mensajes aparecen solo cuando la reserva se confirma?**
   "Guardando reserva &lt;id&gt;" y "Correo enviado a &lt;correo&gt;", en ese orden y antes de `confirmar()`. En los rechazos no se imprime nada.
