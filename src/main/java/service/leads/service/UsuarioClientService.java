package service.leads.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import service.leads.dto.response.OficinaResponse;
import service.leads.dto.response.UsuarioContactoInternalResponse;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UsuarioClientService {

    private final RestClient usuarioClient;


    public UsuarioClientService(
            @Qualifier("usuarioRestClient") RestClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    public List<OficinaResponse> listarOficinas(){
        try {
            return usuarioClient.get()
                    .uri("/oficinas")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OficinaResponse>>() {});
        } catch (RestClientResponseException e) {
            log.warn("No se pudo listar oficinas: {}", e.getStatusCode());
            return List.of();
        }
    }

    public UsuarioContactoInternalResponse buscarContacto(UUID usuarioId){
        try {
            return usuarioClient.get()
                    .uri("/internal/usuario/{id}/contacto", usuarioId)
                    .retrieve()
                    .body(UsuarioContactoInternalResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("No se pudo obtener contacto del usuario {}: {}", usuarioId, e.getStatusCode());
            return null;
        }
    }


}
