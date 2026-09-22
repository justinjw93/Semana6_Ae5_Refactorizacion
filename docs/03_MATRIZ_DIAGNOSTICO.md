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

### E · Clases

- **Clase con demasiadas responsabilidades:** `procesar()` mezcla dominio (validar, calcular) con infraestructura (guardar, notificar). Ver `02_MAPA_RESPONSABILIDADES.md`.
- **Feature Envy:** el servicio pide `getCorreo()`, `getInicio()`, `getFin()` y `getTipo()` para decidir reglas. La regla "fin posterior a inicio" pertenece a la reserva (o a su periodo); el descuento pertenece a una política de precios.
- **Shotgun Surgery potencial:** la validación `contains("@")` sobre un `String` tendrá que repetirse en cada nuevo lugar que reciba correos.

### F · Datos

| Concepto | Tipo actual | Invariante que el tipo no protege |
|---|---|---|
| Correo | `String` | Formato de correo; hoy basta con contener "@". |
| Tipo de reserva | `String` | Conjunto cerrado (NORMAL, VIP); hoy acepta "vip", `null` o cualquier texto. |
| Periodo | `LocalDateTime` × 2 | fin posterior a inicio; hoy se valida fuera de `Reserva`. |
| Total | `double` | Dinero con precisión decimal y moneda. |
| Anticipación | `int` suelto | Coherencia con `inicio`; hoy puede contradecir la fecha real. |

- **Long Parameter List:** el constructor de `Reserva` recibe 5 parámetros, 3 de ellos `String` intercambiables (id, correo, tipo): un error de orden compila sin aviso.

### G · Condicionales

| Condicional | Línea | Riesgo de mantenimiento |
|---|---|---|
| `r == null` | L18 | Oculta errores de programación detrás de un 0. |
| correo null o sin "@" | L22–23 | Regla débil; tentación de "mejorarla" como si fuera refactorización. |
| periodo null o fin no posterior | L27–29 | Tres condiciones en una; la regla es del dato, no del servicio. |
| `horasAnticipacion < 2` | L33 | Número mágico y dato no derivado de `inicio`. |
| `"VIP".equals(tipo)` | L39 | Variante con más probabilidad de crecer (más tipos o descuentos). |

### H · Testabilidad

| Dependencia | Dónde | Por qué dificulta probar |
|---|---|---|
| Persistencia simulada | L43–45 | `println`: solo se verifica capturando la consola. |
| Notificación simulada | L47–49 | Igual; no se puede sustituir por un doble. |
| Reloj del sistema | `Main.java` L12–13 | Resultados dependientes de la fecha de ejecución. |
| Descuento acoplado a validaciones | L37–41 | Probar solo el cálculo exige construir una reserva que pase las cuatro guardas. |

No se usan mocks en este laboratorio; solo se identifican las dependencias.

> **Bug vs. smell:** "vip" en minúsculas (EX-01) y el correo "@" (EX-03) son *comportamientos dudosos* (posibles bugs) que se documentan sin corregir. El smell es *Primitive Obsession*, que es lo que los permite.

Evita diagnósticos genéricos como:

- "código feo"
- "mala práctica"
- "viola SOLID"

Debes indicar **dónde**, **qué ocurre**, **qué riesgo produce** y **cómo podrías verificar el cambio**.
