# Fase I | Matriz de diagnóstico obligatoria

Completa al menos cinco filas con evidencias reales.

Las líneas se refieren a `src/main/java/edu/uees/refactor/service/ServicioReservas.java` salvo que se indique otro archivo. Los IDs LB-/EX- remiten a `01_LINEA_BASE.md`.

| # | Ubicación | Smell / problema | Categoría | Impacto | Candidato | Prueba necesaria |
|---:|---|---|---|---|---|---|
| 1 | `procesar()` L14–54 | Clase/método con demasiadas responsabilidades: valida, calcula, persiste, notifica y confirma | Clase | Cuatro razones de cambio en un mismo método; tocar una regla obliga a reverificar todo el flujo | Extract Method → Extract Class | LB-01…LB-06 caracterizados |
| 2 | L43–49 | Efectos externos acoplados (`System.out.println` para guardar y notificar) | Testabilidad | Persistencia y correo no se pueden verificar ni sustituir sin capturar la consola | Separar persistencia y notificación en colaboradores inyectables | `reservaValidaSeConfirma()`, `rechazoNoGeneraEfectos()` |
| 3 | `Reserva.correo` (`Reserva.java` L8) · validación L22–25 | Primitive Obsession (correo como `String`) | Datos | Se acepta `"@"` como correo válido (EX-03); la regla `contains("@")` se duplicará en cada punto que reciba correos (Shotgun Surgery potencial) | Introducir Value Object `Correo` | `correoInvalidoNoProcesa()`, `correoSoloArrobaSeAceptaHoy()` |
| 4 | `Reserva.inicio/fin` (`Reserva.java` L9–10) · validación L27–31 | Data Clumps + Feature Envy: el servicio compara `getFin()` con `getInicio()` | Datos | La invariante "fin posterior a inicio" vive fuera del dato; `Reserva` puede existir con un periodo inválido | Introducir `PeriodoReserva` | `periodoInvalidoNoProcesa()` |
| 5 | `Reserva.tipo` (`Reserva.java` L11) · L39 | Primitive Obsession (tipo) + condicional de variante `"VIP".equals(...)` | Datos / Condicional | `"vip"` o `null` se cobran como NORMAL (EX-01, EX-02); cada nuevo tipo añade otro `if` | Enum `TipoReserva` + extraer cálculo de tarifa | `vipConservaResultadoActual()`, `tipoEnMinusculasNoAplicaDescuento()` |
| 6 | L18–35 | Retorno `0` como señal de error en cuatro guardas distintas | Condicional / contrato | El llamador no distingue el motivo del rechazo; un error de programación (`null`, EX-07) se oculta en un 0 | Nombrar las validaciones con métodos de intención (sin cambiar el contrato todavía) | `unaHoraNoPermiteProcesar()`, `reservaNulaRetornaCero()` |
| 7 | L14–16 · L33 | Dato desconectado: `horasAnticipacion` llega como parámetro aparte y no se deriva de `inicio` | Datos | Se confirma una reserva en el pasado si se declaran 5 h (EX-06) | Derivar la anticipación de `inicio` y un reloj (cambio funcional, posterior) | `dosHorasSiPermiteProcesar()` |
| 8 | L37 · L40 · L33 | Números mágicos: `40`, `0.85`, `2` | Condicional | La tarifa, el descuento y la anticipación mínima no tienen nombre ni un único punto de cambio | Extraer constantes con nombre | `normalRetornaTarifaBase()`, `vipConservaResultadoActual()` |
| 9 | `Main.java` L12–13 | Dependencia del reloj del sistema (`LocalDateTime.now()`) | Testabilidad | La ejecución no es reproducible entre días; impide fijar un periodo exacto en pruebas | Inyectar la fecha o un `Clock` en el punto de entrada | Arnés `LineaBaseManual` con fecha fija |

## Notas de diagnóstico (Fases E–H)

### Fase E · Diagnóstico de clases

#### 8.1 Long Class / demasiadas responsabilidades

| Señal | ¿Aparece? | Evidencia |
|---|---|---|
| Responsabilidades de dominio e infraestructura mezcladas | Sí | `procesar()` valida y calcula (dominio, L18–41) y en el mismo método imprime "Guardando" y "Correo enviado" (infraestructura, L43–49). |
| Varios motivos para cambiar | Sí | Cuatro: reglas de validación, política de precio, persistencia y notificación (ver `02_MAPA_RESPONSABILIDADES.md`). |
| Método principal con demasiadas decisiones | Sí | Cinco condicionales (L18, L22, L27, L33, L39), dos efectos y un cambio de estado en un único método de 40 líneas. |
| Dependencias futuras difíciles de aislar | Sí | Persistencia y correo están escritos como `System.out.println` dentro del método; no hay punto donde sustituirlos por una BD o SMTP sin editar `procesar()`. |

#### 8.2 Feature Envy

`ServicioReservas` llama a `r.getCorreo()` (L22–23), `r.getInicio()` y `r.getFin()` (L27–29) y `r.getTipo()` (L39) para decidir reglas. No todo debe moverse a `Reserva`:

| Conocimiento | ¿De quién es realmente? | Motivo |
|---|---|---|
| fin posterior a inicio | De la reserva (o de un `PeriodoReserva`) | Es una invariante del dato: una reserva con periodo inválido no debería poder existir. |
| Correo con formato válido | De un concepto `Correo` | La regla es igual para cualquier objeto que tenga correo, no solo `Reserva`. |
| Descuento VIP 15 % | De una política de precios | Cambia por decisión comercial, no por la naturaleza de la reserva. |
| Anticipación mínima de 2 h | De una política de reservas | Es una regla de negocio configurable, externa al dato. |

#### 8.3 Shotgun Surgery potencial

Hoy cada regla aparece una sola vez, así que el riesgo es **potencial**. Si mañana otro servicio (cancelaciones, recordatorios) recibe el correo como `String` y las fechas como dos `LocalDateTime` sueltos, tendrá que repetir `contains("@")` y `isAfter(...)`. Un cambio en la regla de correo obligaría entonces a editar cada copia.

### Fase F · Diagnóstico de datos

#### 9.1 Primitive Obsession

| Dato actual | Concepto posible | Regla que podría justificarlo | ¿Justificado hoy? |
|---|---|---|---|
| `String correo` | `Correo` | Formato, normalización, no vacío. | Sí: hoy `"@"` pasa como válido (EX-03) y la regla vive en el servicio. |
| `String tipo` | `TipoReserva` (enum) | Valores permitidos. | Sí: acepta `"vip"`, `null` o cualquier texto y los cobra como NORMAL (EX-01, EX-02). |
| `LocalDateTime inicio` + `fin` | `PeriodoReserva` | fin > inicio. | Sí: la invariante existe y hoy se valida fuera del dato (L27–31). |
| `double total` | `Dinero` | No negativo, moneda, redondeo. | Todavía no: solo existen dos valores (40.0 y 34.0), exactos en `double`, y ninguna operación suma importes. Se revisará si aparecen sumas o monedas. |

#### 9.2 Data Clumps

`inicio` y `fin` se declaran juntos (`Reserva.java` L9–10), se reciben juntos en el constructor y se validan juntos (L27–31) con una única regla. Es la señal de Data Clumps que justifica `PeriodoReserva`.

#### 9.3 Long Parameter List

`new Reserva(id, correo, inicio, fin, tipo)` recibe cinco parámetros. Tres son `String` intercambiables (id, correo, tipo) y dos son `LocalDateTime` intercambiables (inicio, fin). Un error de orden compila sin aviso: `new Reserva("R-1", "VIP", inicio, fin, "ana@uees.edu.ec")` se acepta y termina rechazado por correo inválido sin explicar por qué. Agrupar inicio y fin en un periodo reduciría la lista a cuatro parámetros de tipos distintos.

Además, `horasAnticipacion` llega como parámetro aparte de `procesar()` y no se deriva de `inicio`. Puede contradecir la fecha real: EX-06 confirma una reserva de 2020.

### Fase G · Diagnóstico de condicionales

| Condición | Regla expresada | Riesgo de mantenimiento |
|---|---|---|
| `r == null` (L18) | No procesar ausencia de reserva | Oculta un error de programación detrás de un 0 (EX-07); el llamador no se entera. |
| correo null / sin @ (L22–23) | Correo inválido | Regla débil; la tentación de "mejorarla" es un cambio funcional disfrazado de refactorización. |
| fin <= inicio (L27–29) | Periodo inválido | Tres condiciones en una; la regla pertenece al dato y se repetirá en cada flujo que use fechas. |
| horas < 2 (L33) | Anticipación insuficiente | Número mágico `2`; el dato no se deriva de `inicio` (EX-06). |
| tipo == VIP (L39) | Aplicar descuento | Distingue mayúsculas (EX-01); cada nuevo tipo o descuento añade otro `if` al mismo método. |

Las cuatro guardas de validación devuelven el mismo `0`, así que el llamador no sabe **qué** falló.

#### 10.1 ¿Merece refactorización cada condicional?

| Pregunta | Respuesta con evidencia |
|---|---|
| ¿La condición expresa una regla de negocio con nombre propio? | Sí: "correo válido", "periodo válido", "anticipación mínima" y "descuento VIP" son reglas con nombre, pero en el código son expresiones anónimas. |
| ¿La condición se repite? | Hoy no; el riesgo es que se repita en futuros servicios (Shotgun Surgery potencial). |
| ¿Oculta el flujo principal? | Sí: las primeras 18 líneas del método son guardas; el cálculo y los efectos quedan al final. |
| ¿Mezcla validación con cálculo y efectos? | Sí: validación (L18–35), cálculo (L37–41) y efectos (L43–51) están en el mismo método. |
| ¿La variante probablemente crecerá? | Sí para `"VIP".equals(...)`: es la condición que más probablemente crezca con nuevos tipos o descuentos. |

### Fase H · Evaluar testabilidad

| Zona | Qué sería deseable probar | Qué lo dificulta hoy |
|---|---|---|
| Descuento VIP | Retorno calculado (34.0) | Mezclado con validaciones y efectos: hay que construir una reserva que pase las cuatro guardas (L18–35) e imprime en consola. |
| Correo | Que se solicite notificación | Solo existe `println` (L47–49); no hay objeto que verificar. |
| Persistencia | Que se guarde una reserva | Solo existe `println` (L43–45). |
| Periodo | Regla fin > inicio | La regla vive dentro del servicio (L27–31), no se puede probar de forma aislada. |
| Punto de entrada | Resultado reproducible | `Main` usa `LocalDateTime.now()` (`Main.java` L12–13); el arnés de línea base lo evita con una fecha fija. |

No se implementan mocks todavía: solo se identifican las dependencias y efectos que dificultan probar.

> **Bug vs. smell:** "vip" en minúsculas (EX-01) y el correo "@" (EX-03) son *comportamientos dudosos* (posibles bugs) que se documentan sin corregir. El smell es *Primitive Obsession*, que es lo que los permite.

Evita diagnósticos genéricos como:

- "código feo"
- "mala práctica"
- "viola SOLID"

Debes indicar **dónde**, **qué ocurre**, **qué riesgo produce** y **cómo podrías verificar el cambio**.
