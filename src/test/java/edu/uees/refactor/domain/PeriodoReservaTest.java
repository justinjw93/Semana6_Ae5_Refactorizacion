package edu.uees.refactor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoReservaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 1, 10, 0);

    @Test
    @DisplayName("Fin posterior a inicio es válido")
    void finPosteriorEsValido() {
        // Arrange
        PeriodoReserva periodo = new PeriodoReserva(INICIO, INICIO.plusHours(1));
        // Act / Assert
        assertTrue(periodo.esValido());
    }

    @Test
    @DisplayName("Fin igual a inicio no es válido (borde de LB-04)")
    void finIgualNoEsValido() {
        assertFalse(new PeriodoReserva(INICIO, INICIO).esValido());
    }

    @Test
    @DisplayName("Fin anterior a inicio no es válido (EX-04)")
    void finAnteriorNoEsValido() {
        assertFalse(new PeriodoReserva(INICIO, INICIO.minusHours(1)).esValido());
    }

    @Test
    @DisplayName("Fechas nulas no son válidas y no lanzan excepción")
    void fechasNulasNoSonValidas() {
        assertDoesNotThrow(() -> new PeriodoReserva(null, INICIO));
        assertFalse(new PeriodoReserva(null, INICIO).esValido());
        assertFalse(new PeriodoReserva(INICIO, null).esValido());
    }
}
