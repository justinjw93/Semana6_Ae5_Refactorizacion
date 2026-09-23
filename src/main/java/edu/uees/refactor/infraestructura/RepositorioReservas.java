package edu.uees.refactor.infraestructura;

import edu.uees.refactor.domain.Reserva;

/**
 * Persistencia de reservas. Por ahora es simulada con la consola,
 * igual que en el código heredado.
 */
public class RepositorioReservas {

    public void guardar(Reserva reserva) {
        System.out.println("Guardando reserva " + reserva.getId());
    }
}
