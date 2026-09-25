package service.leads.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import service.leads.dto.internal.EstadoComercial;
import service.leads.dto.internal.RolUsuario;
import service.leads.dto.request.CrearLeadRequest;
import service.leads.dto.response.AgenteInternalResponse;
import service.leads.dto.response.LeadResponse;
import service.leads.dto.response.OficinaResponse;
import service.leads.dto.response.PropiedadCoordenadasResponse;
import service.leads.entity.DistributionRules;
import service.leads.entity.Estado;
import service.leads.entity.Lead;
import service.leads.exceptions.AccesoNoAutorizadoException;
import service.leads.exceptions.PropiedadNoEncontradaException;
import service.leads.exceptions.SinAgentesDisponiblesException;
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

    private final UsuarioClientService usuarioClientService;
    private final AgenteClientService agenteClientService;
    private final PropiedadClientService propiedadClientService;
    private final LeadRepository leadRepository;
    private final DistributionRulesRepository distributionRulesRepository;

    public LeadResponse crear(UUID clienteId, RolUsuario rolSolicitante, CrearLeadRequest leadRequest){

        if(rolSolicitante!=RolUsuario.CLIENTE)
            throw new AccesoNoAutorizadoException("Acceso no autorizado");

        PropiedadCoordenadasResponse coordenadasResponse = propiedadClientService.buscarCoordenadas(leadRequest.propiedadId());

        if(coordenadasResponse==null || coordenadasResponse.estadoComercial()!= EstadoComercial.DISPONIBLE)
            throw new PropiedadNoEncontradaException("Propiedad no encontrada");


        Lead lead = Lead.builder()
                .propiedadId(leadRequest.propiedadId())
                .clientId(clienteId)
                .agenteId(null)
                .estado(Estado.CONTACTADO)
                .build();

        Lead guardado=  leadRepository.save(lead);
        log.info("lead creado");
        UUID agenteIdAsignado = asignarAgente(coordenadasResponse.latitud(),coordenadasResponse.longitud());
        guardado.setAgenteId(agenteIdAsignado);
        log.info("lead asignado");
        leadRepository.save(guardado);

        return LeadResponse.from(guardado);
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


}
