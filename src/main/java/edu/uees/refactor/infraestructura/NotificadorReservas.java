package edu.uees.refactor.infraestructura;

import edu.uees.refactor.domain.Reserva;

/**
 * Notificación al cliente. Por ahora es simulada con la consola,
 * igual que en el código heredado.
 */
public class NotificadorReservas {

    public void notificarConfirmacion(Reserva reserva) {
        System.out.println("Correo enviado a " + reserva.getCorreo());
    }
}
