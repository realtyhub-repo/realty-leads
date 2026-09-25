package service.leads.dto.response;

import java.util.UUID;


public record AgenteInternalResponse(
        UUID id,
        UUID usuarioId,
        UUID oficinaId,
        String nombre

) {
}
