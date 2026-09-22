# Fase K | Proponer pruebas antes de proponer código

Las pruebas solo se nombran y describen; se implementarán con JUnit 5 y AAA en el Laboratorio 2. Cada fila de `04_MATRIZ_RIESGO.md` tiene al menos una prueba protectora.

| Refactorización candidata | Comportamiento a proteger | Prueba propuesta |
|---|---|---|
| Extraer cálculo VIP | VIP conserva el resultado actual: retorna 34.0 (LB-02) | vipConservaResultadoActual() |
| Extraer cálculo VIP | NORMAL retorna la tarifa base 40.0 (LB-01) | normalRetornaTarifaBase() |
| Extraer cálculo VIP / Enum TipoReserva | `"vip"` en minúsculas hoy retorna 40.0, sin descuento (EX-01) | tipoEnMinusculasNoAplicaDescuento() |
| Simplificar validación | 1h retorna 0 y no confirma (LB-06) | unaHoraNoPermiteProcesar() |
| Simplificar validación | 2h sí confirma: el límite es inclusivo (LB-05) | dosHorasSiPermiteProcesar() |
| Simplificar validación | `procesar(null, 5)` retorna 0 sin excepción (EX-07) | reservaNulaRetornaCero() |
| Separar notificación | Reserva válida sigue confirmándose (LB-01) | reservaValidaSeConfirma() |
| Separar notificación | Los mensajes "Guardando" y "Correo enviado" solo aparecen si se confirma (LB-03, LB-04, LB-06) | rechazoNoGeneraEfectos() |
| Introducir periodo | Periodo inválido sigue rechazándose: retorna 0 y queda PENDIENTE (LB-04, EX-04) | periodoInvalidoNoProcesa() |
| Introducir Correo | `"incorrecto"` retorna 0 y queda PENDIENTE (LB-03) | correoInvalidoNoProcesa() |
| Introducir Correo | `"@"` hoy se acepta y confirma (EX-03); cambiarlo sería una decisión funcional explícita | correoSoloArrobaSeAceptaHoy() |

## Mapa riesgo → pruebas

| Cambio de la matriz de riesgo | Riesgo | Pruebas protectoras |
|---|---|---|
| Separar cálculo VIP | Bajo | vipConservaResultadoActual, normalRetornaTarifaBase, tipoEnMinusculasNoAplicaDescuento |
| Simplificar validaciones | Medio | unaHoraNoPermiteProcesar, dosHorasSiPermiteProcesar, reservaNulaRetornaCero |
| Extraer clase de notificación | Medio | reservaValidaSeConfirma, rechazoNoGeneraEfectos |
| Introducir PeriodoReserva | Alto | periodoInvalidoNoProcesa |
| Introducir Correo | Alto | correoInvalidoNoProcesa, correoSoloArrobaSeAceptaHoy |

Las pruebas que dependen de la fecha usarán una fecha fija (como `LineaBaseManual`), no `LocalDateTime.now()`. Las que verifican mensajes capturarán `System.out`, porque hoy no existe otro punto de observación. No se usan mocks en esta etapa.

> Primero define qué comportamiento necesitas proteger; después decide cómo reorganizar la estructura.
