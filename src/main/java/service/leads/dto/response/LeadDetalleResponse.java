package service.leads.dto.response;

import lombok.Builder;
import service.leads.entity.Estado;
import service.leads.entity.Lead;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LeadDetalleResponse(
        UUID id,
        UUID propiedadId,
        Estado estado,

        UUID clienteId,
        String nombreCliente,
        String telefonoCliente,
        String emailCliente,

        UUID agenteId,

        LocalDateTime createdAt
) {
    public static LeadDetalleResponse from(Lead lead, UsuarioContactoInternalResponse contacto) {
        return LeadDetalleResponse.builder()
                .id(lead.getId())
                .propiedadId(lead.getPropiedadId())
                .estado(lead.getEstado())
                .clienteId(lead.getClienteId())
                .nombreCliente(contacto.nombre())
                .telefonoCliente(contacto.telefono())
                .emailCliente(contacto.email())
                .agenteId(lead.getAgenteId())
                .createdAt(lead.getCreatedAt())
                .build();
    }
}
