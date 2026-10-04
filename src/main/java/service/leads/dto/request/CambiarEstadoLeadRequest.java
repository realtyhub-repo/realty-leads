package service.leads.dto.request;

import jakarta.validation.constraints.NotNull;
import service.leads.entity.Estado;

public record CambiarEstadoLeadRequest(

        @NotNull
        Estado nuevoEstado

) {
}
