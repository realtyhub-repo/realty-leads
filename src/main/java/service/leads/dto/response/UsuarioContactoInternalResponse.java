package service.leads.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UsuarioContactoInternalResponse(

        UUID id,
        String nombre,
        String telefono,
        String email

) {

}
