package edu.uees.refactor.service;

import edu.uees.refactor.domain.EstadoReserva;
import edu.uees.refactor.domain.Reserva;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de caracterización de ServicioReservas (red de seguridad de Ae5).
 *
 * Fijan el comportamiento observable ACTUAL (retorno, estado y mensajes),
 * incluso cuando parece incorrecto (EX-01, EX-03, EX-06). Cambiar esas
 * reglas sería un cambio funcional, no una refactorización.
 *
 * Los mensajes se verifican capturando System.out porque todavía no existe
 * otro punto de observación. No se usan mocks.
 */
class ServicioReservasTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(1);
    private static final String CORREO_VALIDO = "ana@uees.edu.ec";
    private static final double DELTA = 0.0001;

    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private ServicioReservas servicio;

    @BeforeEach
    void capturarConsola() {
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
        servicio = new ServicioReservas();
    }

    @AfterEach
    void restaurarConsola() {
        System.setOut(salidaOriginal);
    }

    private String consola() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    private static Reserva reserva(String id, String correo, LocalDateTime inicio,
                                   LocalDateTime fin, String tipo) {
        return new Reserva(id, correo, inicio, fin, tipo);
    }

    private void assertRechazada(double total, Reserva r) {
        assertEquals(0.0, total, DELTA);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals("", consola(), "Una reserva rechazada no debe guardar ni notificar");
    }

    // ---------- Tarifa ----------

    @Test
    @DisplayName("LB-01 NORMAL válida retorna la tarifa base 40.0")
    void normalRetornaTarifaBase() {
        // Arrange
        Reserva r = reserva("R-001", CORREO_VALIDO, INICIO, FIN, "NORMAL");
        // Act
        double total = servicio.procesar(r, 5);
        // Assert
        assertEquals(40.0, total, DELTA);
    }

    @Test
    @DisplayName("LB-02 VIP válida conserva el resultado actual 34.0")
    void vipConservaResultadoActual() {
        Reserva r = reserva("R-002", CORREO_VALIDO, INICIO, FIN, "VIP");

        double total = servicio.procesar(r, 5);

        assertEquals(34.0, total, DELTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    @DisplayName("EX-01 \"vip\" en minúsculas no aplica descuento")
    void tipoEnMinusculasNoAplicaDescuento() {
        Reserva r = reserva("R-101", CORREO_VALIDO, INICIO, FIN, "vip");

        double total = servicio.procesar(r, 5);

        assertEquals(40.0, total, DELTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    @DisplayName("EX-02 tipo null se cobra como NORMAL")
    void tipoNuloSeCobraComoNormal() {
        Reserva r = reserva("R-102", CORREO_VALIDO, INICIO, FIN, null);

        double total = servicio.procesar(r, 5);

        assertEquals(40.0, total, DELTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    // ---------- Confirmación y efectos ----------

    @Test
    @DisplayName("LB-01 reserva válida queda CONFIRMADA")
    void reservaValidaSeConfirma() {
        Reserva r = reserva("R-001", CORREO_VALIDO, INICIO, FIN, "NORMAL");

        servicio.procesar(r, 5);

        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    @DisplayName("Reserva válida imprime guardar y luego correo, en ese orden")
    void reservaValidaGuardaYLuegoNotifica() {
        Reserva r = reserva("R-001", CORREO_VALIDO, INICIO, FIN, "NORMAL");

        servicio.procesar(r, 5);

        String[] lineas = consola().strip().split("\\R");
        assertEquals(2, lineas.length);
        assertEquals("Guardando reserva R-001", lineas[0]);
        assertEquals("Correo enviado a ana@uees.edu.ec", lineas[1]);
    }

    // ---------- Validaciones ----------

    @Test
    @DisplayName("LB-03 correo sin @ retorna 0, queda PENDIENTE y no genera efectos")
    void correoInvalidoNoProcesa() {
        Reserva r = reserva("R-003", "incorrecto", INICIO, FIN, "NORMAL");

        double total = servicio.procesar(r, 5);

        assertRechazada(total, r);
    }

    @Test
    @DisplayName("EX-05 correo null retorna 0 sin excepción")
    void correoNuloNoProcesa() {
        Reserva r = reserva("R-105", null, INICIO, FIN, "NORMAL");

        double total = servicio.procesar(r, 5);

        assertRechazada(total, r);
    }

    @Test
    @DisplayName("EX-03 correo \"@\" hoy se acepta y confirma")
    void correoSoloArrobaSeAceptaHoy() {
        Reserva r = reserva("R-103", "@", INICIO, FIN, "NORMAL");

        double total = servicio.procesar(r, 5);

        assertEquals(40.0, total, DELTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    @DisplayName("LB-04 periodo con fin igual a inicio no se procesa")
    void periodoInvalidoNoProcesa() {
        Reserva r = reserva("R-004", CORREO_VALIDO, INICIO, INICIO, "NORMAL");

        double total = servicio.procesar(r, 5);

        assertRechazada(total, r);
    }

    @Test
    @DisplayName("EX-04 periodo con fin antes de inicio no se procesa")
    void periodoInvertidoNoProcesa() {
        Reserva r = reserva("R-104", CORREO_VALIDO, INICIO, INICIO.minusHours(1), "NORMAL");

        double total = servicio.procesar(r, 5);

        assertRechazada(total, r);
    }

    @Test
    @DisplayName("Inicio o fin null no se procesan y no lanzan excepción")
    void periodoConFechasNulasNoProcesa() {
        Reserva sinInicio = reserva("R-108", CORREO_VALIDO, null, FIN, "NORMAL");
        Reserva sinFin = reserva("R-109", CORREO_VALIDO, INICIO, null, "NORMAL");

        double totalSinInicio = servicio.procesar(sinInicio, 5);
        double totalSinFin = servicio.procesar(sinFin, 5);

        assertEquals(0.0, totalSinInicio, DELTA);
        assertEquals(0.0, totalSinFin, DELTA);
        assertEquals(EstadoReserva.PENDIENTE, sinInicio.getEstado());
        assertEquals(EstadoReserva.PENDIENTE, sinFin.getEstado());
        assertEquals("", consola());
    }

    @Test
    @DisplayName("LB-05 dos horas de anticipación sí permiten procesar (límite inclusivo)")
    void dosHorasSiPermiteProcesar() {
        Reserva r = reserva("R-005", CORREO_VALIDO, INICIO, FIN, "NORMAL");

        double total = servicio.procesar(r, 2);

        assertEquals(40.0, total, DELTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    @DisplayName("LB-06 una hora de anticipación retorna 0 y no confirma")
    void unaHoraNoPermiteProcesar() {
        Reserva r = reserva("R-006", CORREO_VALIDO, INICIO, FIN, "NORMAL");

        double total = servicio.procesar(r, 1);

        assertRechazada(total, r);
    }

    @Test
    @DisplayName("EX-06 inicio en el pasado con 5 h declaradas se confirma (la anticipación la declara el llamador)")
    void inicioEnPasadoConHorasDeclaradasSeConfirma() {
        LocalDateTime pasado = LocalDateTime.of(2020, 1, 1, 10, 0);
        Reserva r = reserva("R-106", CORREO_VALIDO, pasado, pasado.plusHours(1), "NORMAL");

        double total = servicio.procesar(r, 5);

        assertEquals(40.0, total, DELTA);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }

    @Test
    @DisplayName("EX-07 procesar(null) retorna 0 sin excepción ni efectos")
    void reservaNulaRetornaCero() {
        double total = servicio.procesar(null, 5);

        assertEquals(0.0, total, DELTA);
        assertEquals("", consola());
    }

    @Test
    @DisplayName("Varias reglas inválidas a la vez: se rechaza sin efectos")
    void variasReglasInvalidasNoGeneranEfectos() {
        Reserva r = reserva("R-107", "incorrecto", INICIO, INICIO, "VIP");

        double total = servicio.procesar(r, 1);

        assertRechazada(total, r);
        assertTrue(consola().isEmpty());
    }
}
