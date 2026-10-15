package service.leads.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import service.leads.dto.internal.ErrorResponse;
import service.leads.dto.request.ReservarVisitaRequest;
import service.leads.dto.response.AgentesOcupadosResponse;
import service.leads.dto.response.VisitaReservadaResponse;
import service.leads.exceptions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class VisitaClientService {

    private final RestClient visitasClient;

    public VisitaClientService(
            @Qualifier("visitasRestClient") RestClient visitasClient) {
        this.visitasClient = visitasClient;
    }

    /** Ids de los agentes (de la lista) que ya tienen una visita que se cruza con esa hora. */
    public List<UUID> consultarAgentesOcupados(List<UUID> agenteIds, LocalDateTime fechaHora) {
        try {
            AgentesOcupadosResponse respuesta = visitasClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/visitas/agentes-ocupados")
                            .queryParam("agenteIds", agenteIds.toArray())
                            .queryParam("fechaHora", fechaHora.toString())
                            .build())
                    .retrieve()
                    .body(AgentesOcupadosResponse.class);

            if (respuesta == null || respuesta.agentesIds() == null) {
                return List.of();
            }
            return respuesta.agentesIds();

        } catch (RestClientException e) {
            // Fallar cerrado: si no sabemos quién está ocupado, no asumimos que todos están libres.
            throw traducir(e);
        }
    }

    public VisitaReservadaResponse reservar(ReservarVisitaRequest request) {
        try {
            VisitaReservadaResponse respuesta = visitasClient.post()
                    .uri("/internal/visitas/reservar")
                    .body(request)
                    .retrieve()
                    .body(VisitaReservadaResponse.class);

            if (respuesta == null) {
                throw new VisitasNoDisponibleException("Visitas respondió sin datos de la reserva");
            }
            return respuesta;

        } catch (RestClientException e) {
            throw traducir(e);
        }
    }

    /**
     * Compensación: cancela la visita pendiente del lead.
     * Es best-effort: si falla solo se registra, porque ya estamos manejando otro error.
     */
    public void cancelarPorLead(UUID leadId) {
        try {
            visitasClient.delete()
                    .uri("/internal/visitas/lead/{leadId}", leadId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("No se pudo cancelar la visita del lead {} (compensación): {}", leadId, e.getMessage());
        }
    }

    /**
     * Convierte el error de Visitas en una excepción de Lead según el "codigo" que manda.
     * Cualquier cosa que no reconozcamos (5xx, caída de red) es "Visitas no disponible".
     */
    private RuntimeException traducir(RestClientException e) {
        if (e instanceof RestClientResponseException respuestaError) {
            ErrorResponse error = leerError(respuestaError);
            String codigo = error != null ? error.codigo() : null;
            String mensaje = (error != null && error.mensaje() != null)
                    ? error.mensaje()
                    : "Error al comunicarse con el servicio de visitas";

            if (codigo != null) {
                switch (codigo) {
                    case "AGENTE_OCUPADO":
                        return new AgenteOcupadoException(mensaje);
                    case "PROPIEDAD_OCUPADA":
                        return new PropiedadOcupadaException(mensaje);
                    case "VISITA_YA_EXISTE":
                        return new VisitaYaExisteException(mensaje);
                    case "FECHA_INVALIDA":
                    case "FUERA_DE_HORARIO":
                        return new FechaVisitaInvalidaException(mensaje);
                    default:
                        break;
                }
            }
            log.error("Visitas respondió {} sin un código conocido: {}",
                    respuestaError.getStatusCode(), respuestaError.getResponseBodyAsString());
        } else {
            log.error("No se pudo contactar al servicio de visitas: {}", e.getMessage());
        }
        return new VisitasNoDisponibleException("El servicio de visitas no está disponible, intenta más tarde");
    }

    private ErrorResponse leerError(RestClientResponseException e) {
        try {
            return e.getResponseBodyAs(ErrorResponse.class);
        } catch (Exception ignorado) {
            return null;
        }
    }
}
