package service.leads.event.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record LeadCreadoEvent(
        UUID leadId,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId,
        UUID visitaId,
        LocalDateTime fechaHoraVisita
) {
}
