package service.leads.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI leadsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RealtyHub - Microservicio de Leads")
                        .version("v1")
                        .description("Gestión de leads (contactos de clientes interesados en una propiedad): "
                                + "creación con asignación automática de agente y visita, agendamiento de visitas, "
                                + "cambio de estado y consulta. La identidad del usuario llega desde el gateway "
                                + "en los headers X-User-Id y X-User-Role."));
    }
}
