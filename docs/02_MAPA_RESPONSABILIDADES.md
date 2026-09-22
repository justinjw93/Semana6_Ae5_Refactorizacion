# Fase D | Mapa actual de responsabilidades

| Fragmento | Ubicación | Responsabilidad observada | Clase actual |
|---|---|---|---|
| Validar null/correo/periodo/anticipación | `ServicioReservas.java` L18–35 | Validación | ServicioReservas |
| Calcular total y descuento VIP | `ServicioReservas.java` L37–41 | Cálculo de tarifa | ServicioReservas |
| Imprimir "Guardando reserva" | `ServicioReservas.java` L43–45 | Persistencia simulada | ServicioReservas |
| Imprimir "Correo enviado" | `ServicioReservas.java` L47–49 | Notificación simulada | ServicioReservas |
| Cambiar estado a CONFIRMADA | `ServicioReservas.java` L51 → `Reserva.java` L28–30 | Cambio de estado de dominio | Reserva |

Detalle de las guardas de validación en `ServicioReservas.procesar()`:

| Línea | Regla | Retorno si falla |
|---|---|---|
| L18–20 | `r != null` | 0 |
| L22–25 | correo no null y contiene "@" | 0 |
| L27–31 | inicio y fin no null, fin posterior a inicio | 0 |
| L33–35 | `horasAnticipacion >= 2` | 0 |

## Mapa conceptual

```text
ServicioReservas
├── valida entrada
├── interpreta correo
├── interpreta periodo
├── decide anticipación
├── calcula precio
├── conoce descuento VIP
├── simula persistencia
├── simula notificación
└── ordena confirmar Reserva

Reserva
└── mantiene estado
```

**Pregunta clave:** ¿cuántas razones diferentes podría tener `ServicioReservas` para cambiar?

**Respuesta:** al menos cuatro, cada una con un responsable distinto en el negocio:

1. **Reglas de validación** (L18–35): por ejemplo, exigir un formato de correo real, cambiar la anticipación mínima de 2 h o limitar la duración de la tutoría.
2. **Política de precio y descuento** (L37–41): la tarifa base 40, el 15 % VIP o la aparición de nuevos tipos de reserva.
3. **Mecanismo de persistencia** (L43–45): hoy es un `println`; mañana puede ser una base de datos o un repositorio.
4. **Canal de notificación** (L47–49): hoy es un `println`; mañana puede ser SMTP, SMS o una cola de mensajes.

Una quinta razón, menor, es el **orden del flujo** (guardar → notificar → confirmar, L43–51). Si se decide notificar solo después de persistir con éxito, también cambia este método.

Todas estas razones convergen en un único método de 40 líneas (`procesar()`, L14–54). Por eso cualquier cambio en una regla obliga a volver a verificar las demás.
