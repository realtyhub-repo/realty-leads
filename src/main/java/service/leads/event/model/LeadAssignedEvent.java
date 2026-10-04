package service.leads.event.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LeadAssignedEvent(

    UUID leadId,
    UUID propiedadId,
    UUID clienteId,
    UUID agenteId
) {
}
