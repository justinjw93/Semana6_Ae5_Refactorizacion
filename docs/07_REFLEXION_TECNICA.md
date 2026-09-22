# Reflexión técnica final

Extensión sugerida: **250–350 palabras**.

<!-- reflexion:inicio -->
El problema de mayor riesgo es que `procesar()` concentra validación, cálculo de tarifa, persistencia y notificación en un solo método. Tiene al menos cuatro razones de cambio, y cualquier ajuste en una regla obliga a reverificar las demás. Además, los efectos son `println`, así que no existe un punto donde observar o sustituir la persistencia y el correo sin capturar la consola.

El problema que parece más fácil de corregir es la validación de correo y el tipo como `String`. Introducir un `Correo` o un enum `TipoReserva` parece mecánico, pero la línea base demuestra que hoy `"@"` confirma la reserva y `"vip"` en minúsculas se cobra 40.0. Una validación "mejor" cambiaría esos resultados; eso sería un cambio funcional disfrazado de refactorización.

Antes de tocar el código son indispensables los seis escenarios de línea base con fecha fija, las pruebas de los límites de 1 h y 2 h, y una prueba que confirme que un rechazo no imprime mensajes. También conviene fijar los casos dudosos (EX-01, EX-03, EX-07) para que cualquier cambio en ellos sea deliberado.

La primera responsabilidad que movería es el cálculo de tarifa, extrayéndolo a un método con intención y constantes con nombre. La evidencia que usaría para defenderlo es que LB-01 y LB-02 lo caracterizan por completo (40.0 y 34.0), que el cambio es local a cuatro líneas y que la matriz de riesgo lo clasifica como Bajo. Separar persistencia y notificación vendría después, porque afecta efectos observables.

La diferencia entre refactorizar y hacer un cambio funcional está en lo observable. Refactorizar cambia la estructura interna (un `if`, un método, una clase) y preserva el retorno, el estado final y los mensajes. Un cambio funcional altera alguno de esos resultados, como aceptar "vip" o rechazar "@", y debe declararse como tal. Este laboratorio me dejó claro que el código "funciona" solo respecto a una línea base que primero hay que medir.
<!-- reflexion:fin -->

## Checklist

- [x] Proyecto base compila y ejecuta.
- [x] Seis escenarios de línea base.
- [x] Mapa de responsabilidades.
- [x] Mínimo cinco problemas diagnosticados.
- [x] Matriz de riesgo.
- [x] Pruebas propuestas.
- [x] Plan priorizado.
- [x] Commit Git del estado inicial.
- [x] Reflexión técnica.
