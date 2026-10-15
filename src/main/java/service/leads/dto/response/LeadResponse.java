package service.leads.dto.response;

import lombok.Builder;
import service.leads.entity.Estado;
import service.leads.entity.Lead;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LeadResponse(
        UUID id,
        UUID propiedadId,
        UUID clienteId,
        Estado estado,
        UUID agenteId,
        UUID visitaId,
        LocalDateTime fechaHoraVisita
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


    public static LeadResponse from(Lead lead, VisitaReservadaResponse visita) {
        return LeadResponse.builder()
                .id(lead.getId())
                .propiedadId(lead.getPropiedadId())
                .clienteId(lead.getClienteId())
                .estado(lead.getEstado())
                .agenteId(lead.getAgenteId())
                .visitaId(visita.id())
                .fechaHoraVisita(visita.fechaHora())
                .build();
    }
}
