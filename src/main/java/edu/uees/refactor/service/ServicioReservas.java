package edu.uees.refactor.service;

import edu.uees.refactor.domain.Reserva;
import edu.uees.refactor.domain.TipoReserva;
import edu.uees.refactor.infraestructura.NotificadorReservas;
import edu.uees.refactor.infraestructura.RepositorioReservas;

public class ServicioReservas {

    static final int HORAS_MINIMAS_ANTICIPACION = 2;

    private static final double SIN_PROCESAR = 0;

    private final CalculadoraTarifa calculadora;
    private final RepositorioReservas repositorio;
    private final NotificadorReservas notificador;

    public ServicioReservas() {
        this(new CalculadoraTarifa(), new RepositorioReservas(), new NotificadorReservas());
    }

    public ServicioReservas(CalculadoraTarifa calculadora,
                            RepositorioReservas repositorio,
                            NotificadorReservas notificador) {
        this.calculadora = calculadora;
        this.repositorio = repositorio;
        this.notificador = notificador;
    }

    public double procesar(
            Reserva r,
            int horasAnticipacion) {

        if (!esProcesable(r, horasAnticipacion)) {
            return SIN_PROCESAR;
        }

        double total = calculadora.calcular(TipoReserva.desde(r.getTipo()));

        repositorio.guardar(r);
        notificador.notificarConfirmacion(r);
        r.confirmar();

        return total;
    }

    private boolean esProcesable(Reserva r, int horasAnticipacion) {
        return r != null
                && esCorreoValido(r)
                && r.getPeriodo().esValido()
                && tieneAnticipacionSuficiente(horasAnticipacion);
    }

    private boolean esCorreoValido(Reserva r) {
        return r.getCorreo() != null
                && r.getCorreo().contains("@");
    }

    private boolean tieneAnticipacionSuficiente(int horasAnticipacion) {
        return horasAnticipacion >= HORAS_MINIMAS_ANTICIPACION;
    }
}
