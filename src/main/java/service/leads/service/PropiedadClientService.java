package service.leads.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import service.leads.dto.response.PropiedadCoordenadasResponse;

import java.util.UUID;

@Service
public class PropiedadClientService {

    private final RestClient propiedadClient;

    public PropiedadClientService(
            @Qualifier("propiedadRestClient") RestClient usuarioClient) {
        this.propiedadClient = usuarioClient;
    }

    public PropiedadCoordenadasResponse buscarCoordenadas(UUID propiedadId){
        return propiedadClient.get()
                .uri("/internal/propiedades/{id}", propiedadId)
                .retrieve()
                .body(PropiedadCoordenadasResponse.class);
    }

}
