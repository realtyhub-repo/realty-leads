package service.leads.dto.response;

import lombok.Builder;
import service.leads.dto.internal.EstadoComercial;

@Builder
public record PropiedadCoordenadasResponse(
        EstadoComercial estadoComercial,
        Double latitud,
        Double longitud) {
}
