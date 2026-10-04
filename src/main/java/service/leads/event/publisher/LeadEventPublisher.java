package service.leads.event.publisher;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import service.leads.config.RabbitConfig;
import service.leads.entity.Lead;
import service.leads.event.model.LeadAssignedEvent;

@Component
@RequiredArgsConstructor
public class LeadEventPublisher {



    private final RabbitTemplate rabbitTemplate;


    public void publicarLeadAsignado(Lead lead) {

        String exchange = RabbitConfig.TOPIC_EXCHANGE_NAME;
        String routingKey = RabbitConfig.ROUTING_KEY;

        LeadAssignedEvent assignedEvent = LeadAssignedEvent.builder()
                .leadId(lead.getId())
                .propiedadId(lead.getPropiedadId())
                .clienteId(lead.getClienteId())
                .agenteId(lead.getAgenteId())
                .build();


        rabbitTemplate.convertAndSend(exchange, routingKey, assignedEvent);

    }


}
