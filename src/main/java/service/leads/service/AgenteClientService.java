package service.leads.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import service.leads.dto.response.AgenteInternalResponse;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AgenteClientService {


    private final RestClient usuarioClient;


    public AgenteClientService(
            @Qualifier("usuarioRestClient") RestClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }


    public List<AgenteInternalResponse> listarAgentesInterno(List<UUID> oficinaIds){
        try {
            return usuarioClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/agentes")
                            .queryParam("oficinaIds", oficinaIds.toArray())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AgenteInternalResponse>>() {});
        } catch (RestClientResponseException e) {
            log.warn("No se pudo listar agentes internos: {}", e.getStatusCode());
            return List.of();
        }

    }

}
