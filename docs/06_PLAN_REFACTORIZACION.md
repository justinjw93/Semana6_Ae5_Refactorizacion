# Fase L | Plan priorizado de refactorización

No implementes todavía.

Criterio de orden: de menor a mayor riesgo (`04_MATRIZ_RIESGO.md`), con la red de pruebas creciendo antes de cada cambio.

| Orden | Cambio | Por qué primero / después | Pruebas requeridas | Dependencias |
|---:|---|---|---|---|
| 1 | Caracterizar LB-01…LB-06 y EX-01…EX-07 con JUnit 5 | Sin red de seguridad, todo cambio posterior es a ciegas. No toca código de producción, así que su riesgo es nulo. | Las 11 propuestas en `05_PRUEBAS_PROPUESTAS.md` | — |
| 2 | Extraer métodos con intención (`calcularTotal`, `esCorreoValido`, `esPeriodoValido`, `tieneAnticipacionSuficiente`) y constantes (`TARIFA_BASE`, `DESCUENTO_VIP`, `HORAS_MINIMAS`) | Riesgo bajo y local: no cambia firmas ni construcción de objetos. Da nombre a las reglas y prepara la variación del tipo. | vipConservaResultadoActual, normalRetornaTarifaBase, unaHoraNoPermiteProcesar, dosHorasSiPermiteProcesar, reservaNulaRetornaCero | 1 |
| 3 | Separar persistencia y notificación en clases propias, inyectadas en `ServicioReservas` | Quita las dos razones de cambio de infraestructura y habilita dobles de prueba cuando se introduzcan más adelante. Riesgo medio porque son efectos observables; debe conservar el orden y los textos. | reservaValidaSeConfirma, rechazoNoGeneraEfectos | 1, 2 |
| 4 | Introducir `PeriodoReserva` y `TipoReserva` | Invariantes claras (fin > inicio; valores permitidos), pero cambian la construcción de `Reserva` y de `Main`. Va después de 3 para que el servicio ya sea pequeño al adaptar el constructor. Mantener "vip" = 40.0 salvo decisión funcional explícita. | periodoInvalidoNoProcesa, tipoEnMinusculasNoAplicaDescuento, vipConservaResultadoActual | 1, 2 |
| 5 | Introducir `Correo` | Mayor riesgo de convertirse en cambio funcional ("@" dejaría de ser válido). Al final, con toda la red en su lugar y con una decisión documentada sobre la regla. | correoInvalidoNoProcesa, correoSoloArrobaSeAceptaHoy | 1, 4 |

## Fuera de este plan (cambios funcionales)

Estos puntos aparecieron en la línea base, pero corregirlos cambia comportamiento observable. Requieren una decisión explícita y no son refactorización:

- Aceptar `"vip"` en minúsculas como VIP (EX-01).
- Rechazar correos como `"@"` (EX-03).
- Derivar `horasAnticipacion` de `inicio` para no confirmar reservas en el pasado (EX-06).
- Sustituir el retorno 0 por excepciones o un resultado con motivo de rechazo.

## Ejemplo de razonamiento

1. Caracterizar casos actuales.
2. Extraer cálculo a método con intención.
3. Separar notificación / persistencia.
4. Introducir Value Objects.

La evaluación se centra en **la justificación**, no en repetir exactamente este orden.
