package edu.uees.refactor.domain;

/**
 * Tipos de reserva con su factor de precio.
 *
 * Conserva la regla heredada: solo el texto exacto "VIP" es VIP;
 * cualquier otro valor ("vip", "PREMIUM", null) se trata como NORMAL.
 */
public enum TipoReserva {
    NORMAL(1.0),
    VIP(0.85);

    private final double factorPrecio;

    TipoReserva(double factorPrecio) {
        this.factorPrecio = factorPrecio;
    }

    public double factorPrecio() {
        return factorPrecio;
    }

    public static TipoReserva desde(String texto) {
        return "VIP".equals(texto) ? VIP : NORMAL;
    }
}
