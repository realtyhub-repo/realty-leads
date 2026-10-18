package service.leads.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import service.leads.config.RabbitConfig;
import service.leads.dto.response.VisitaReservadaResponse;
import service.leads.entity.Lead;
import service.leads.event.model.LeadCreadoEvent;
import service.leads.event.model.LeadDescartadoEvent;
import service.leads.event.model.LeadOfertaAceptadaEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarCreado(Lead lead, VisitaReservadaResponse visita) {
        LeadCreadoEvent evento = new LeadCreadoEvent(
                lead.getId(),
                lead.getPropiedadId(),
                lead.getClienteId(),
                lead.getAgenteId(),
                visita.id(),
                visita.fechaHora());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.TOPIC_EXCHANGE_NAME,
                    RabbitConfig.RK_LEAD_CREADO,
                    evento);
        } catch (AmqpException e) {
            log.error("No se pudo publicar lead.creado del lead {}: {}", lead.getId(), e.getMessage());
        }
    }

    public void publicarOfertaAceptada(Lead lead) {
        LeadOfertaAceptadaEvent evento = new LeadOfertaAceptadaEvent(
                lead.getId(),
                lead.getPropiedadId(),
                lead.getClienteId(),
                lead.getAgenteId());

        rabbitTemplate.convertAndSend(
                RabbitConfig.TOPIC_EXCHANGE_NAME,
                RabbitConfig.RK_LEAD_OFERTA_ACEPTADA,
                evento);
    }

    public void publicarDescartado(Lead lead) {
        LeadDescartadoEvent evento = new LeadDescartadoEvent(
                lead.getId(),
                lead.getPropiedadId(),
                lead.getClienteId(),
                lead.getAgenteId());

        rabbitTemplate.convertAndSend(
                RabbitConfig.TOPIC_EXCHANGE_NAME,
                RabbitConfig.RK_LEAD_DESCARTADO,
                evento);
    }
}
