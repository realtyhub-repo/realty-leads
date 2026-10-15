package service.leads.event.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import service.leads.config.RabbitConfig;
import service.leads.entity.Lead;
import service.leads.event.model.LeadOfertaAceptadaEvent;

@Component
@RequiredArgsConstructor
public class LeadEventPublisher {

    private final RabbitTemplate rabbitTemplate;

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
}
