package edu.uees.refactor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoReservaTest {

    @Test
    @DisplayName("Solo el texto exacto \"VIP\" es VIP")
    void soloVipExactoEsVip() {
        assertEquals(TipoReserva.VIP, TipoReserva.desde("VIP"));
    }

    @Test
    @DisplayName("\"vip\", \"PREMIUM\", \"NORMAL\" y null se tratan como NORMAL (regla heredada)")
    void otrosValoresSonNormal() {
        assertEquals(TipoReserva.NORMAL, TipoReserva.desde("vip"));
        assertEquals(TipoReserva.NORMAL, TipoReserva.desde("PREMIUM"));
        assertEquals(TipoReserva.NORMAL, TipoReserva.desde("NORMAL"));
        assertEquals(TipoReserva.NORMAL, TipoReserva.desde(null));
    }
}
