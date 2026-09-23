package edu.uees.refactor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 1, 10, 0);

    @Test
    @DisplayName("El constructor conserva los datos y la reserva nace PENDIENTE")
    void constructorConservaDatos() {
        // Arrange / Act
        Reserva r = new Reserva("R-001", "ana@uees.edu.ec", INICIO, INICIO.plusHours(1), "VIP");

        // Assert
        assertEquals("R-001", r.getId());
        assertEquals("ana@uees.edu.ec", r.getCorreo());
        assertEquals(INICIO, r.getInicio());
        assertEquals(INICIO.plusHours(1), r.getFin());
        assertEquals("VIP", r.getTipo());
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    }

    @Test
    @DisplayName("confirmar() cambia el estado a CONFIRMADA")
    void confirmarCambiaEstado() {
        Reserva r = new Reserva("R-001", "ana@uees.edu.ec", INICIO, INICIO.plusHours(1), "NORMAL");

        r.confirmar();

        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
    }
}
