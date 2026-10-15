package service.leads.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendarVisitaRequest(
        @NotNull
        LocalDateTime fechaHora
) {}
