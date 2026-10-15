package service.leads.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrearLeadRequest(
        @NotNull
        UUID propiedadId,

        @NotNull
        LocalDateTime fechaHora
) {}
