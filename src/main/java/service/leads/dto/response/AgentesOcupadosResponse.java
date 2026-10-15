package service.leads.dto.response;

import java.util.List;
import java.util.UUID;

public record AgentesOcupadosResponse(
        List<UUID> agentesIds
) {}
