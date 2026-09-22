package edu.uees.refactor.app;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.service.ServicioReservas;

import java.time.LocalDateTime;

/**
 * Arnés temporal de evidencia para la línea base (Laboratorio 1).
 *
 * Solo observa el comportamiento actual: no modifica
 * ServicioReservas ni Reserva. Usa una fecha fija en lugar
 * de LocalDateTime.now() para que la salida sea reproducible.
 */
public class LineaBaseManual {

    private static final LocalDateTime INICIO =
            LocalDateTime.of(2026, 10, 1, 10, 0);

    private static final String CORREO_VALIDO =
            "ana@uees.edu.ec";

    public static void main(String[] args) {

        System.out.println("=== Escenarios de línea base ===");

        ejecutar("LB-01", "NORMAL válida",
                new Reserva("R-001", CORREO_VALIDO, INICIO, INICIO.plusHours(1), "NORMAL"), 5);

        ejecutar("LB-02", "VIP válida",
                new Reserva("R-002", CORREO_VALIDO, INICIO, INICIO.plusHours(1), "VIP"), 5);

        ejecutar("LB-03", "Correo inválido",
                new Reserva("R-003", "incorrecto", INICIO, INICIO.plusHours(1), "NORMAL"), 5);

        ejecutar("LB-04", "Periodo inválido (fin = inicio)",
                new Reserva("R-004", CORREO_VALIDO, INICIO, INICIO, "NORMAL"), 5);

        ejecutar("LB-05", "Límite válido (2 h)",
                new Reserva("R-005", CORREO_VALIDO, INICIO, INICIO.plusHours(1), "NORMAL"), 2);

        ejecutar("LB-06", "Límite inválido (1 h)",
                new Reserva("R-006", CORREO_VALIDO, INICIO, INICIO.plusHours(1), "NORMAL"), 1);

        System.out.println();
        System.out.println("=== Escenarios extra (reglas implícitas) ===");

        ejecutar("EX-01", "Tipo \"vip\" en minúsculas",
                new Reserva("R-101", CORREO_VALIDO, INICIO, INICIO.plusHours(1), "vip"), 5);

        ejecutar("EX-02", "Tipo null",
                new Reserva("R-102", CORREO_VALIDO, INICIO, INICIO.plusHours(1), null), 5);

        ejecutar("EX-03", "Correo \"@\"",
                new Reserva("R-103", "@", INICIO, INICIO.plusHours(1), "NORMAL"), 5);

        ejecutar("EX-04", "Fin antes de inicio",
                new Reserva("R-104", CORREO_VALIDO, INICIO, INICIO.minusHours(1), "NORMAL"), 5);

        ejecutar("EX-05", "Correo null",
                new Reserva("R-105", null, INICIO, INICIO.plusHours(1), "NORMAL"), 5);

        ejecutar("EX-06", "Inicio en el pasado con 5 h declaradas",
                new Reserva("R-106", CORREO_VALIDO,
                        LocalDateTime.of(2020, 1, 1, 10, 0),
                        LocalDateTime.of(2020, 1, 1, 11, 0), "NORMAL"), 5);

        ejecutarNula();
    }

    private static void ejecutar(
            String id,
            String escenario,
            Reserva reserva,
            int horasAnticipacion) {

        System.out.println();
        System.out.println("[" + id + "] " + escenario
                + " | tipo=" + reserva.getTipo()
                + " | correo=" + reserva.getCorreo()
                + " | horas=" + horasAnticipacion);

        try {
            double total = new ServicioReservas()
                    .procesar(reserva, horasAnticipacion);
            System.out.println("  -> Estado: " + reserva.getEstado());
            System.out.println("  -> Retorno: " + total);
        } catch (RuntimeException e) {
            System.out.println("  -> Excepción: " + e);
        }
    }

    private static void ejecutarNula() {

        System.out.println();
        System.out.println("[EX-07] procesar(null, 5)");

        try {
            double total = new ServicioReservas()
                    .procesar(null, 5);
            System.out.println("  -> Retorno: " + total);
        } catch (RuntimeException e) {
            System.out.println("  -> Excepción: " + e);
        }
    }
}
