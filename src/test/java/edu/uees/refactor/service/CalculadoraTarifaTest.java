package edu.uees.refactor.service;

import edu.uees.refactor.domain.TipoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraTarifaTest {

    private final CalculadoraTarifa calculadora = new CalculadoraTarifa();

    @Test
    @DisplayName("NORMAL cobra la tarifa base 40.0")
    void normalCobraTarifaBase() {
        assertEquals(40.0, calculadora.calcular(TipoReserva.NORMAL), 0.0001);
    }

    @Test
    @DisplayName("VIP cobra 34.0 (15 % de descuento)")
    void vipCobraConDescuento() {
        assertEquals(34.0, calculadora.calcular(TipoReserva.VIP), 0.0001);
    }
}
