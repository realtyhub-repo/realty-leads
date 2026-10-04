package service.leads.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import service.leads.dto.internal.EstadoComercial;
import service.leads.dto.internal.RolUsuario;
import service.leads.dto.request.CrearLeadRequest;
import service.leads.dto.response.*;
import service.leads.entity.DistributionRules;
import service.leads.entity.Estado;
import service.leads.entity.Lead;
import service.leads.event.publisher.LeadEventPublisher;
import service.leads.exceptions.*;
import service.leads.repository.DistributionRulesRepository;
import service.leads.repository.LeadRepository;
import service.leads.util.GeoUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private static final int N_OFICINAS_CERCANAS = 5;
    private static final String INDICE_LEAD_ACTIVO = "idx_un_lead_activo_por_cliente_propiedad";

    private final UsuarioClientService usuarioClientService;
    private final AgenteClientService agenteClientService;
    private final PropiedadClientService propiedadClientService;
    private final LeadRepository leadRepository;
    private final DistributionRulesRepository distributionRulesRepository;
    private final LeadEventPublisher leadEventPublisher;

    public LeadResponse crear(UUID clienteId, RolUsuario rolSolicitante, CrearLeadRequest leadRequest){

        if(rolSolicitante!=RolUsuario.CLIENTE)
            throw new AccesoNoAutorizadoException("Acceso no autorizado");

        List<Estado> estados = List.of(Estado.DESCARTADO, Estado.CERRADO);

        boolean existeLead = leadRepository.existsByClienteIdAndPropiedadIdAndEstadoNotIn(clienteId,leadRequest.propiedadId(), estados);

        if(existeLead){
            throw new LeadDuplicadoException("Ya tienes un contacto activo para esta propiedad");
        }

        PropiedadCoordenadasResponse coordenadasResponse = propiedadClientService.buscarCoordenadas(leadRequest.propiedadId());

        if(coordenadasResponse==null || coordenadasResponse.estadoComercial()!= EstadoComercial.DISPONIBLE)
            throw new PropiedadNoEncontradaException("Propiedad no encontrada");


        Lead lead = Lead.builder()
                .propiedadId(leadRequest.propiedadId())
                .clienteId(clienteId)
                .agenteId(null)
                .estado(Estado.CONTACTADO)
                .build();

        Lead guardado;
        try {
            guardado = leadRepository.save(lead);
        } catch (DataIntegrityViolationException e) {
            if (esDuplicadoDeLeadActivo(e))
                throw new LeadDuplicadoException("Ya tienes un contacto activo para esta propiedad");
            throw e;
        }

        try {
            UUID agenteIdAsignado = asignarAgente(coordenadasResponse.latitud(), coordenadasResponse.longitud());
            guardado.setAgenteId(agenteIdAsignado);
            guardado =  leadRepository.save(guardado);
        } catch (SinAgentesDisponiblesException e) {
            log.warn("Lead {} creado sin agente asignado: {}", guardado.getId(), e.getMessage());
            return LeadResponse.from(guardado);

        }
        try {
            leadEventPublisher.publicarLeadAsignado(guardado);
        } catch (AmqpException e) {
            log.error("Lead {} asignado pero no se pudo publicar el evento: {}", guardado.getId(), e.getMessage());
        }

        return LeadResponse.from(guardado);
    }

    public void cambiarEstado(UUID leadId,Estado nuevoEstado, RolUsuario rolSolicitante, UUID solicitanteId){
        Lead leadPorId = buscarLeadPorId(leadId);

        boolean esDueno = leadPorId.getAgenteId()!=null && leadPorId.getAgenteId().equals(solicitanteId);
        boolean esAdmin = rolSolicitante==RolUsuario.ADMINISTRADOR_CENTRAL;

        if(!esAdmin && !esDueno)
            throw new AccesoNoAutorizadoException("Acceso no autorizado");

        if(leadPorId.getEstado()==Estado.CERRADO || leadPorId.getEstado()==Estado.DESCARTADO)
            throw new EstadoTerminalException("El lead ya fue "+leadPorId.getEstado());

        leadPorId.setEstado(nuevoEstado);
        leadRepository.save(leadPorId);

        if(nuevoEstado==Estado.CERRADO)
            log.info("lead.setEstado(nuevoEstado)");
    }

    public List<LeadResponse> listarMisLeads(UUID idAgente, Estado estadoFiltrado){

        if(estadoFiltrado!=null)
            return leadRepository.findByAgenteIdAndEstado(idAgente, estadoFiltrado).stream()
                    .map(LeadResponse::from)
                    .toList();

        return leadRepository.findByAgenteId(idAgente).stream()
                .map(LeadResponse::from)
                .toList();

    }

    public LeadDetalleResponse obtenerDetalle(UUID leadId, RolUsuario rol, UUID solicitanteId) {
        Lead lead = buscarLeadPorId(leadId);

        boolean esDueno = lead.getAgenteId() != null && lead.getAgenteId().equals(solicitanteId);
        boolean esAdmin = rol == RolUsuario.ADMINISTRADOR_CENTRAL;

        if (!esDueno && !esAdmin)
            throw new AccesoNoAutorizadoException("No tienes permiso sobre este lead");

        UsuarioContactoInternalResponse contacto = usuarioClientService.buscarContacto(lead.getClienteId());

        return LeadDetalleResponse.from(lead, contacto);
    }
    private Lead buscarLeadPorId(UUID id){
        return leadRepository.findById(id).orElseThrow(()->
                    new LeadNoEncontradoException("Lead no encontrado")
                );
    }

    private UUID asignarAgente(double latPropiedad, double lonPropiedad){
        List<OficinaResponse> oficinas = usuarioClientService.listarOficinas();

        Map<UUID,Double> distanciaPorOficina = oficinas.stream()
                .collect(Collectors.toMap(
                        OficinaResponse::getId,
                        o-> GeoUtils.calcularDistancia(latPropiedad,lonPropiedad,o.getLatitud(),o.getLongitud())
                ));

        List<UUID> oficinasIdsCercanas = distanciaPorOficina.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(N_OFICINAS_CERCANAS)
                .map(Map.Entry::getKey)
                .toList();

        List<AgenteInternalResponse> candidatos = agenteClientService.listarAgentesInterno(oficinasIdsCercanas);

        if(candidatos.isEmpty())
            throw new SinAgentesDisponiblesException("No hay agentes disponibles para asignar");

        DistributionRules reglas = distributionRulesRepository.findAll().getFirst();

        return candidatos.stream()
                .max(Comparator.comparingDouble(a->calcularPuntaje(a, distanciaPorOficina, reglas)))
                .map(AgenteInternalResponse::usuarioId)
                .orElseThrow();
    }

    private double calcularPuntaje(AgenteInternalResponse agente, Map<UUID, Double> distanciaPorOficina, DistributionRules reglas){
        double distancia = distanciaPorOficina.get(agente.oficinaId());
        double scoreProximidad = 1.0/(1.0+distancia);

        long cargaActual = leadRepository.countByAgenteIdAndEstadoNotIn(
                agente.usuarioId(), List.of(Estado.CERRADO,Estado.DESCARTADO));

        double scoreCarga = 1.0 / (1.0+cargaActual);

        long totalLeads = leadRepository.countByAgenteId(agente.usuarioId());
        long leadsGanados = leadRepository.countByAgenteIdAndEstado(agente.usuarioId(),Estado.CERRADO);
        double scoreConversion = totalLeads==0?0.5:(double) leadsGanados/totalLeads;

        return reglas.getPesoZona()*scoreProximidad
                +reglas.getPesoCarga()*scoreCarga
                +reglas.getPesoConversion()*scoreConversion;

    }


    private boolean esDuplicadoDeLeadActivo(DataIntegrityViolationException e) {
        String mensaje = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        return mensaje != null && mensaje.contains(INDICE_LEAD_ACTIVO);
    }


}
