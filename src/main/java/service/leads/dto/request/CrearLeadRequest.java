package service.leads.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrearLeadRequest(
        @NotNull
        UUID propiedadId
) {}