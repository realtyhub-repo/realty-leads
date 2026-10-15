package service.leads.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservarVisitaRequest(
        UUID leadId,
        UUID propiedadId,
        UUID clienteId,
        UUID agenteId,
        LocalDateTime fechaHora
) {}
