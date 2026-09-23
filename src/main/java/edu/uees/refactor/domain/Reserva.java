package edu.uees.refactor.domain;

import java.time.LocalDateTime;

public class Reserva {

    private final String id;
    private final String correo;
    private final PeriodoReserva periodo;
    private final String tipo;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    public Reserva(
            String id,
            String correo,
            LocalDateTime inicio,
            LocalDateTime fin,
            String tipo) {

        this(id, correo, new PeriodoReserva(inicio, fin), tipo);
    }

    public Reserva(
            String id,
            String correo,
            PeriodoReserva periodo,
            String tipo) {

        this.id = id;
        this.correo = correo;
        this.periodo = periodo;
        this.tipo = tipo;
    }

    public void confirmar() {
        estado = EstadoReserva.CONFIRMADA;
    }

    public String getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public PeriodoReserva getPeriodo() {
        return periodo;
    }

    public LocalDateTime getInicio() {
        return periodo.inicio();
    }

    public LocalDateTime getFin() {
        return periodo.fin();
    }

    public String getTipo() {
        return tipo;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}
