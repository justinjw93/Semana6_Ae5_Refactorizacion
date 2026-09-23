# 08 · Reporte técnico — Ae5 Refactorización respaldada por pruebas

**Estudiante:** Justin Arreaga · **Asignatura:** Diseño de Software (UCOM0310) · **Semana 6 · PEL 4 – 2026**
**Repositorio:** https://github.com/justinjw93/Semana6_Ae5_Refactorizacion

## 1. Problema inicial

`ServicioReservas.procesar` hacía todo en un solo método: validaba correo, periodo y anticipación, calculaba el
precio (40 o 34 para VIP), "guardaba" e "enviaba correo" con `System.out.println`, y confirmaba la reserva.
Tenía seis razones de cambio distintas, números mágicos (`2`, `40`, `0.85`), `inicio` y `fin` sueltos y todas
las fallas devolvían `0` (ver `docs/02` y `docs/03`).

## 2. Línea base

| ID | Escenario | Estado | Retorno |
|---|---|---|---|
| LB-01 | NORMAL válida | CONFIRMADA | 40.0 |
| LB-02 | VIP válida | CONFIRMADA | 34.0 |
| LB-03 | Correo inválido | PENDIENTE | 0.0 |
| LB-04 | fin = inicio | PENDIENTE | 0.0 |
| LB-05 | 2 h de anticipación | CONFIRMADA | 40.0 |
| LB-06 | 1 h de anticipación | PENDIENTE | 0.0 |

Reglas implícitas que también se preservaron: `"vip"` y `null` cobran 40 (EX-01, EX-02), `"@"` se acepta (EX-03),
fin antes de inicio se rechaza (EX-04), correo `null` se rechaza (EX-05), una reserva en el pasado se confirma si
el llamador declara 5 h (EX-06) y `procesar(null)` retorna 0 sin excepción (EX-07).

## 3. Red de seguridad

Commit `test: caracterizar comportamiento actual…`: se agregó JUnit 5 y 19 pruebas AAA **sobre el código sin
modificar** (`00_mvn_test_inicial.txt`: 19/19 verde). Cada prueba de rechazo verifica tres cosas: retorno 0,
estado PENDIENTE y que no se imprima nada. Los mensajes se verifican capturando `System.out` (sin mocks).

Antes de refactorizar se comprobó que la suite detecta errores introduciendo cambios temporales (mutaciones) y
revirtiéndolos. Se repitió al final sobre el código refactorizado (`07_mutaciones_regresion.txt`):

| Mutación temporal | Prueba que la detectó |
|---|---|
| Límite `>= 2` cambiado a `> 2` | `dosHorasSiPermiteProcesar` |
| Descuento 0.85 → 0.80 | `vipConservaResultadoActual`, `vipCobraConDescuento` |
| Eliminar `r.confirmar()` | `reservaValidaSeConfirma` y 6 más |
| Aceptar `"vip"` en minúsculas | `tipoEnMinusculasNoAplicaDescuento`, `otrosValoresSonNormal` |
| Aceptar `fin == inicio` | `periodoInvalidoNoProcesa`, `finIgualNoEsValido` |
| No validar `@` | `correoInvalidoNoProcesa` |

## 4. Refactorización 1 — Decompose Conditional + constantes

| Elemento | Detalle |
|---|---|
| Problema de diseño | Cuatro `if` anónimos con doble negación y números mágicos ocultaban el flujo principal; las reglas no tenían nombre. |
| Código antes | `if (r.getCorreo() == null \|\| !r.getCorreo().contains("@")) return 0;` … `if (horasAnticipacion < 2) return 0;` … `double total = 40; if ("VIP".equals(r.getTipo())) total = total * 0.85;` |
| Técnica aplicada | Decompose Conditional (`esProcesable`, `esCorreoValido`, `esPeriodoValido`, `tieneAnticipacionSuficiente`, `calcularTotal`) y Replace Magic Number with Symbolic Constant (`TARIFA_BASE`, `FACTOR_VIP`, `HORAS_MINIMAS_ANTICIPACION`). |
| Prueba que protege | `unaHoraNoPermiteProcesar`, `dosHorasSiPermiteProcesar`, `correoInvalidoNoProcesa`, `periodoInvalidoNoProcesa`, `reservaNulaRetornaCero`, `vipConservaResultadoActual`. |
| Resultado después | `if (!esProcesable(r, horas)) return SIN_PROCESAR;` y luego el camino feliz en 5 líneas. `< 2` pasó a `>= HORAS_MINIMAS` (misma regla, expresada en positivo). 19/19 verde. |
| Commit | `4812409 refactor: descomponer condicionales y nombrar reglas de ServicioReservas` |

## 5. Refactorización 2 — Extract Class (persistencia y notificación)

| Elemento | Detalle |
|---|---|
| Problema de diseño | El servicio mezclaba reglas de negocio con infraestructura (`System.out.println` como "guardar" y "enviar correo"). Cambiar a una base de datos o a SMTP obligaba a tocar la lógica de reservas. |
| Código antes | `System.out.println("Guardando reserva " + r.getId()); System.out.println("Correo enviado a " + r.getCorreo());` dentro de `procesar`. |
| Técnica aplicada | Extract Class: `RepositorioReservas.guardar()` y `NotificadorReservas.notificarConfirmacion()` en el paquete `infraestructura`, recibidas por constructor. Se mantiene un constructor sin argumentos para `Main`. |
| Prueba que protege | `reservaValidaGuardaYLuegoNotifica` (texto y orden exactos), `correoInvalidoNoProcesa` y demás rechazos (sin efectos), `reservaValidaSeConfirma`. Nuevas: `repositorioImprimeGuardado`, `notificadorImprimeCorreo`. |
| Resultado después | `repositorio.guardar(r); notificador.notificarConfirmacion(r); r.confirmar();`. El servicio ya no conoce la consola. 21/21 verde. |
| Commit | `fb3317b refactor: extraer persistencia y notificacion de ServicioReservas` |

## 6. Refactorización 3 — Value Object `PeriodoReserva`

| Elemento | Detalle |
|---|---|
| Problema de diseño | Data Clumps: `inicio` y `fin` viajaban siempre juntos y su regla (`fin > inicio`) vivía en el servicio (Feature Envy). |
| Código antes | Campos `LocalDateTime inicio, fin` en `Reserva`; en el servicio: `r.getInicio() != null && r.getFin() != null && r.getFin().isAfter(r.getInicio())`. |
| Técnica aplicada | Introduce Value Object (`record PeriodoReserva`) + Move Method (`esValido()`). **Decisión clave:** el record no lanza excepción con datos inválidos, porque el contrato actual rechaza esos periodos con `return 0`; lanzar una excepción habría sido un cambio funcional. |
| Prueba que protege | `periodoInvalidoNoProcesa`, `periodoInvertidoNoProcesa`, `periodoConFechasNulasNoProcesa`. Nuevas: 4 pruebas de `PeriodoReservaTest` y `constructorConPeriodo`. |
| Resultado después | `Reserva` guarda un `PeriodoReserva`; conserva el constructor de 5 parámetros y `getInicio()/getFin()`, así que `Main` y `LineaBaseManual` no cambiaron. El servicio usa `r.getPeriodo().esValido()`. 26/26 verde. |
| Commit | `5977ed6 refactor: introducir PeriodoReserva para agrupar inicio y fin` |

## 7. Refactorización 4 (adicional) — `CalculadoraTarifa` + `TipoReserva`

| Elemento | Detalle |
|---|---|
| Problema de diseño | La política de precios (tarifa y descuento) es la parte que más probablemente crecerá (nuevos tipos) y seguía dentro del servicio con un `String` como tipo. |
| Código antes | `if ("VIP".equals(r.getTipo())) return TARIFA_BASE * FACTOR_VIP; return TARIFA_BASE;` |
| Técnica aplicada | Extract Class (`CalculadoraTarifa`) + enum `TipoReserva` con su factor de precio. `TipoReserva.desde()` conserva la regla heredada: solo `"VIP"` exacto es VIP. |
| Prueba que protege | `vipConservaResultadoActual`, `normalRetornaTarifaBase`, `tipoEnMinusculasNoAplicaDescuento`, `tipoNuloSeCobraComoNormal`. Nuevas: `CalculadoraTarifaTest`, `TipoReservaTest`. |
| Resultado después | Agregar un tipo nuevo = una línea en el enum. 30/30 verde. |
| Commit | `25007fe refactor: extraer politica de precios a CalculadoraTarifa y TipoReserva` |

## 8. Comparación antes / después

| Dimensión | Antes | Después | Evidencia |
|---|---|---|---|
| Responsabilidades | `ServicioReservas` validaba, calculaba precio, guardaba, notificaba y confirmaba (6 razones de cambio). | El servicio solo orquesta. Precio → `CalculadoraTarifa`/`TipoReserva`; periodo → `PeriodoReserva`; efectos → `RepositorioReservas`/`NotificadorReservas`. | Commits R2, R3, R4 |
| Cohesión | Baja: un método con reglas de dominio e infraestructura. | Alta: cada clase tiene un solo motivo de cambio. | Estructura de paquetes |
| Acoplamiento | El servicio dependía directamente de la consola. | Depende de colaboradores recibidos por constructor; la consola quedó aislada en `infraestructura`. | `ServicioReservas.java` |
| Datos del dominio | `inicio`/`fin` sueltos; tipo como `String` libre. | `PeriodoReserva` (record con regla propia) y `TipoReserva` (enum con factor de precio). | R3, R4 |
| Condicionales | 5 `if` anónimos con doble negación y números mágicos. | Una guarda `if (!esProcesable(...))` con métodos que nombran cada regla; constantes con nombre. | R1 |
| Pruebas | Ninguna prueba automática. | 30 pruebas JUnit 5 (AAA), todas en verde; 6/6 mutaciones detectadas. | `08_mvn_test_final.txt`, `07_mutaciones_regresion.txt` |
| Git | Commits de diagnóstico. | 1 commit de pruebas + 4 commits de refactorización + 1 de documentación, cada uno con la suite en verde. | `git log` |

Comportamiento funcional: **sin cambios**. La salida de `Main` sigue siendo `Total: 34.0` y la salida completa de
`LineaBaseManual` es idéntica antes y después (`06_linea_base_inicial.txt` vs `06_linea_base_final.txt`).

## 9. Historial Git

```
25007fe refactor: extraer politica de precios a CalculadoraTarifa y TipoReserva
5977ed6 refactor: introducir PeriodoReserva para agrupar inicio y fin
fb3317b refactor: extraer persistencia y notificacion de ServicioReservas
4812409 refactor: descomponer condicionales y nombrar reglas de ServicioReservas
efaeab4 test: caracterizar comportamiento actual de ServicioReservas con JUnit 5
1814786 docs: agregar reflexion tecnica            (Laboratorio 1)
…
```

## 10. Preguntas de defensa

1. **¿Qué comportamiento protegiste antes de la primera refactorización?** Retorno, estado y mensajes de los seis
   escenarios de línea base y de siete reglas implícitas, incluido que un rechazo no imprime nada.
2. **¿Por qué esas refactorizaciones?** Atacan los problemas de mayor impacto del diagnóstico: condicionales
   anónimos (R1), mezcla de infraestructura y negocio (R2), Data Clumps/Feature Envy del periodo (R3) y la
   variación más probable, el tipo de reserva (R4). Siguen el orden de menor a mayor riesgo del plan.
3. **¿Qué prueba habría detectado una regresión concreta?** Si al reescribir la guarda se usaba `> 2` en vez de
   `>= 2`, falla `dosHorasSiPermiteProcesar` (comprobado con la mutación M1).
4. **¿Qué cambió y qué permaneció igual?** Cambió la distribución de responsabilidades en 6 clases; se mantuvieron
   retornos, estados, textos y orden de mensajes, y la API pública (`procesar`, constructor de `Reserva`, getters).
5. **¿Qué evidencia da el historial Git?** Un commit por paso, en el orden del ciclo prueba verde → cambio →
   prueba verde → commit; cada commit indica la técnica y el número de pruebas en verde.
6. **¿Qué costo o riesgo introdujo alguna decisión?** `PeriodoReserva` no valida en el constructor para no cambiar
   el contrato: se sigue pudiendo crear un periodo inválido. Además, el nuevo constructor
   `Reserva(id, correo, PeriodoReserva, tipo)` permite pasar `null` como periodo, caso que el servicio no
   contempla. Capturar `System.out` en las pruebas es frágil; se reemplazará con dobles de prueba más adelante.

## 11. Cambios funcionales pendientes (fuera de la refactorización)

Aceptar `"vip"` en minúsculas, rechazar correos como `"@"`, calcular la anticipación a partir de `inicio`,
impedir reprocesar reservas confirmadas y reemplazar el `0` por un resultado explícito. Cada uno requiere una
decisión del negocio y cambiaría a propósito una prueba de caracterización.

## 12. Conclusiones

La refactorización fue segura porque primero se fijó el comportamiento con pruebas y se comprobó que esas
pruebas detectan errores. Cada cambio fue pequeño y quedó registrado en su propio commit. El diseño final separa
las responsabilidades sin alterar lo que el programa hace.

## 13. Declaración de uso de IA

Se utilizó un asistente de IA (Claude) como apoyo para proponer la estructura de las pruebas, redactar
borradores de código y documentación y revisar el proceso. Las decisiones de diseño, la verificación de cada
paso con `mvn clean test` y la revisión final del contenido son responsabilidad del estudiante.
