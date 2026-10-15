package service.leads.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient usuarioRestClient(@Value("${servicios.usuarios-url}") String usuariosUrl) {
        return RestClient.builder()
                .baseUrl(usuariosUrl)
                .build();
    }

    @Bean
    public RestClient propiedadRestClient(@Value("${servicios.propiedades-url}") String propiedadesUrl) {
        return RestClient.builder()
                .baseUrl(propiedadesUrl)
                .build();
    }

    @Bean
    public RestClient visitasRestClient(@Value("${servicios.visitas-url}") String visitasUrl) {
        return RestClient.builder()
                .baseUrl(visitasUrl)
                .build();
    }
}
