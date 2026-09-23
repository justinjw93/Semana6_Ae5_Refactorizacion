package edu.uees.refactor.service;

import edu.uees.refactor.domain.TipoReserva;

/**
 * Política de precios: tarifa base y descuento según el tipo de reserva.
 */
public class CalculadoraTarifa {

    static final double TARIFA_BASE = 40;

    public double calcular(TipoReserva tipo) {
        return TARIFA_BASE * tipo.factorPrecio();
    }
}
