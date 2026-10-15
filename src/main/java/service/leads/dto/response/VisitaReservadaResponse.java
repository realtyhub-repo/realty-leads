package service.leads.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record VisitaReservadaResponse(
        UUID id,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId,
        UUID leadId,
        LocalDateTime fechaHora,
        LocalDateTime fechaHoraFin,
        String estadoVisita
) {}
