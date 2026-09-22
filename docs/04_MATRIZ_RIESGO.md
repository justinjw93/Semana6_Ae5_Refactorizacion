# Fase J | Matriz de riesgo

| Cambio candidato | Probabilidad de romper | Impacto si rompe | Riesgo | Cómo reducirlo |
|---|---|---|---|---|
| Extraer clase de notificación | Media | Alto | **Medio** | Mantener el orden guardar → notificar → confirmar (L43–51) y el texto exacto de los mensajes. Verificar con `reservaValidaSeConfirma()` y `rechazoNoGeneraEfectos()` capturando la consola. No se toca el retorno. |
| Introducir Correo | Alta | Alto | **Alto** | Es el cambio con más tentación de "mejorar" la validación, lo que sería un cambio funcional. Fijar primero con pruebas que `"incorrecto"` y `null` retornan 0 (LB-03, EX-05) y que `"@"` hoy se acepta (EX-03). Hacerlo al final y con decisión explícita sobre la regla. |
| Introducir PeriodoReserva | Media | Alto | **Alto** | Cambia la construcción de `Reserva` (constructor y `Main`) y puede mover el rechazo de `procesar()` al constructor, alterando el contrato de error (hoy retorna 0 sin excepción). Cubrir antes LB-04 y EX-04 y mantener el retorno 0 para periodos inválidos. |
| Simplificar validaciones | Media | Alto | **Medio** | Conservar el orden de las guardas y el retorno 0 en los cuatro rechazos. Proteger los límites con `unaHoraNoPermiteProcesar()` y `dosHorasSiPermiteProcesar()`, y `null` con `reservaNulaRetornaCero()`. |
| Separar cálculo VIP | Baja | Medio | **Bajo** | Cambio local (L37–41) y comportamiento bien entendido. Probar 40.0 (NORMAL) y 34.0 (VIP) antes y después, más `"vip"` en minúsculas = 40.0 para no corregir sin querer. |

## Justificación de cada nivel

- **Separar cálculo VIP · Bajo.** Solo mueve tres líneas a un método con intención. El cálculo no depende de efectos, y LB-01/LB-02 lo caracterizan por completo.
- **Simplificar validaciones · Medio.** Afecta cuatro decisiones a la vez. Reordenarlas o fusionarlas puede cambiar qué entradas se rechazan, por ejemplo al dejar de tratar `null`.
- **Extraer clase de notificación · Medio.** El impacto es alto porque son efectos externos observables. La probabilidad es media: el cambio es mecánico si se respeta el orden y los textos.
- **Introducir PeriodoReserva · Alto.** Obliga a adaptar la construcción de objetos (criterio Medio de la escala). Además puede alterar el flujo de error si el constructor empieza a lanzar excepciones (criterio Alto).
- **Introducir Correo · Alto.** Altera el contrato observable en cuanto se decida validar mejor. Hoy `"@"` confirma la reserva; una validación real la rechazaría.

## Escala

- **Bajo:** cambio local, comportamiento bien entendido y prueba fácil de crear.
- **Medio:** afecta varias decisiones o requiere adaptar construcción de objetos.
- **Alto:** puede alterar contrato observable, flujos de error o efectos externos.
