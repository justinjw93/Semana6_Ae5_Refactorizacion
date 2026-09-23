package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.infraestructura.NotificadorReservas;
import edu.uees.refactor.infraestructura.RepositorioReservas;

public class ServicioReservas {

    static final double TARIFA_BASE = 40;
    static final double FACTOR_VIP = 0.85;
    static final int HORAS_MINIMAS_ANTICIPACION = 2;

    private static final double SIN_PROCESAR = 0;

    private final RepositorioReservas repositorio;
    private final NotificadorReservas notificador;

    public ServicioReservas() {
        this(new RepositorioReservas(), new NotificadorReservas());
    }

    public ServicioReservas(RepositorioReservas repositorio,
                            NotificadorReservas notificador) {
        this.repositorio = repositorio;
        this.notificador = notificador;
    }

    public double procesar(
            Reserva r,
            int horasAnticipacion) {

        if (!esProcesable(r, horasAnticipacion)) {
            return SIN_PROCESAR;
        }

        double total = calcularTotal(r);

        repositorio.guardar(r);
        notificador.notificarConfirmacion(r);
        r.confirmar();

        return total;
    }

    private boolean esProcesable(Reserva r, int horasAnticipacion) {
        return r != null
                && esCorreoValido(r)
                && esPeriodoValido(r)
                && tieneAnticipacionSuficiente(horasAnticipacion);
    }

    private boolean esCorreoValido(Reserva r) {
        return r.getCorreo() != null
                && r.getCorreo().contains("@");
    }

    private boolean esPeriodoValido(Reserva r) {
        return r.getInicio() != null
                && r.getFin() != null
                && r.getFin().isAfter(r.getInicio());
    }

    private boolean tieneAnticipacionSuficiente(int horasAnticipacion) {
        return horasAnticipacion >= HORAS_MINIMAS_ANTICIPACION;
    }

    private double calcularTotal(Reserva r) {
        if ("VIP".equals(r.getTipo())) {
            return TARIFA_BASE * FACTOR_VIP;
        }
        return TARIFA_BASE;
    }
}
