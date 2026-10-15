package service.leads.event.model;

import java.util.UUID;

public record LeadOfertaAceptadaEvent(
        UUID leadId,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId
) {
}
