package service.leads.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import service.leads.dto.response.PropiedadCoordenadasResponse;

import java.util.UUID;

@Service
@Slf4j
public class PropiedadClientService {

    private final RestClient propiedadClient;

    public PropiedadClientService(
            @Qualifier("propiedadRestClient") RestClient usuarioClient) {
        this.propiedadClient = usuarioClient;
    }

    public PropiedadCoordenadasResponse buscarCoordenadas(UUID propiedadId){
        try {
            return propiedadClient.get()
                    .uri("/internal/propiedades/{id}", propiedadId)
                    .retrieve()
                    .body(PropiedadCoordenadasResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("No se pudo obtener coordenadas de la propiedad {}: {}", propiedadId, e.getStatusCode());
            return null;
        }
    }

}
