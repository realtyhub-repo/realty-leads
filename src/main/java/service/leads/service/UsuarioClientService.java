package service.leads.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import service.leads.dto.response.OficinaResponse;

import java.util.List;

@Service
public class UsuarioClientService {

    private final RestClient usuarioClient;


    public UsuarioClientService(
            @Qualifier("usuarioRestClient") RestClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    public List<OficinaResponse> listarOficinas(){
        return usuarioClient.get()
                .uri("/oficinas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<OficinaResponse>>() {});
    }



}
