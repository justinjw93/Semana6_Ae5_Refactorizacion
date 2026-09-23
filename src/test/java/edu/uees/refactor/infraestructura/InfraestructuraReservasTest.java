package edu.uees.refactor.infraestructura;

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

class InfraestructuraReservasTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 1, 10, 0);

    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final Reserva reserva =
            new Reserva("R-001", "ana@uees.edu.ec", INICIO, INICIO.plusHours(1), "NORMAL");

    @BeforeEach
    void capturarConsola() {
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarConsola() {
        System.setOut(salidaOriginal);
    }

    @Test
    @DisplayName("El repositorio conserva el mensaje de persistencia heredado")
    void repositorioImprimeGuardado() {
        // Act
        new RepositorioReservas().guardar(reserva);
        // Assert
        assertEquals("Guardando reserva R-001", salida.toString(StandardCharsets.UTF_8).strip());
    }

    @Test
    @DisplayName("El notificador conserva el mensaje de correo heredado")
    void notificadorImprimeCorreo() {
        new NotificadorReservas().notificarConfirmacion(reserva);

        assertEquals("Correo enviado a ana@uees.edu.ec", salida.toString(StandardCharsets.UTF_8).strip());
    }
}
