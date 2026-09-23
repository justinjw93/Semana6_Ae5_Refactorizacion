package edu.uees.refactor.domain;

import java.time.LocalDateTime;

/**
 * Periodo de una reserva: inicio y fin siempre viajan juntos
 * y comparten la regla "fin posterior a inicio".
 *
 * No lanza excepción con datos inválidos: el contrato actual de
 * ServicioReservas rechaza esos periodos retornando 0, y eso se conserva.
 */
public record PeriodoReserva(LocalDateTime inicio, LocalDateTime fin) {

    public boolean esValido() {
        return inicio != null
                && fin != null
                && fin.isAfter(inicio);
    }
}
