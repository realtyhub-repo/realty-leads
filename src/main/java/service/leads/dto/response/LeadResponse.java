package service.leads.dto.response;

import lombok.Builder;
import service.leads.entity.Estado;
import service.leads.entity.Lead;

import java.util.UUID;

@Builder
public record LeadResponse(
        UUID id,
        UUID propiedadId,
        UUID clienteId,
        Estado estado,
        UUID agenteId
) {
    public static LeadResponse from(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .propiedadId(lead.getPropiedadId())
                .clienteId(lead.getClienteId())
                .estado(lead.getEstado())
                .agenteId(lead.getAgenteId())
                .build();
    }
}
