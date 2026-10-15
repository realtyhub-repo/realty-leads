package service.leads.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import service.leads.dto.internal.EstadoComercial;
import service.leads.dto.internal.RolUsuario;
import service.leads.dto.request.AgendarVisitaRequest;
import service.leads.dto.request.CrearLeadRequest;
import service.leads.dto.request.ReservarVisitaRequest;
import service.leads.dto.response.*;
import service.leads.entity.DistributionRules;
import service.leads.entity.Estado;
import service.leads.entity.Lead;
import service.leads.exceptions.*;
import service.leads.repository.DistributionRulesRepository;
import service.leads.repository.LeadRepository;
import service.leads.util.GeoUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private static final int N_OFICINAS_CERCANAS = 5;
    private static final String INDICE_LEAD_ACTIVO = "idx_un_lead_activo_por_cliente_propiedad";
    private static final List<Estado> ESTADOS_TERMINALES = List.of(Estado.DESCARTADO, Estado.CERRADO);

    private final UsuarioClientService usuarioClientService;
    private final AgenteClientService agenteClientService;
    private final PropiedadClientService propiedadClientService;
    private final VisitaClientService visitaClientService;
    private final LeadRepository leadRepository;
    private final DistributionRulesRepository distributionRulesRepository;

    private record Candidatos(List<AgenteInternalResponse> agentes, Map<UUID, Double> distanciaPorOficina) {
    }


    public LeadResponse crear(UUID clienteId, RolUsuario rolSolicitante, CrearLeadRequest request) {

        if (rolSolicitante != RolUsuario.CLIENTE)
            throw new AccesoNoAutorizadoException("Acceso no autorizado");

        boolean existeLead = leadRepository.existsByClienteIdAndPropiedadIdAndEstadoNotIn(
                clienteId, request.propiedadId(), ESTADOS_TERMINALES);

        if (existeLead)
            throw new LeadDuplicadoException("Ya tienes un contacto activo para esta propiedad");

        PropiedadCoordenadasResponse coordenadas = propiedadClientService.buscarCoordenadas(request.propiedadId());

        if (coordenadas == null || coordenadas.estadoComercial() != EstadoComercial.DISPONIBLE)
            throw new PropiedadNoEncontradaException("Propiedad no encontrada");


        Candidatos zona = buscarCandidatos(coordenadas.latitud(), coordenadas.longitud());


        List<AgenteInternalResponse> libres = filtrarLibres(zona.agentes(), request.fechaHora());


        List<AgenteInternalResponse> ranking = ordenarPorPuntaje(libres, zona.distanciaPorOficina());


        UUID leadId = UUID.randomUUID();
        VisitaReservadaResponse visita = reservarConPrimerAgenteLibre(leadId, clienteId, request, ranking);


        Lead lead = Lead.builder()
                .id(leadId)
                .propiedadId(request.propiedadId())
                .clienteId(clienteId)
                .agenteId(visita.agenteId())
                .estado(Estado.VISITA_AGENDADA)
                .build();

        Lead guardado = guardarCompensando(lead);

        return LeadResponse.from(guardado, visita);
    }

    public LeadResponse agendarVisita(UUID leadId, UUID solicitanteId, RolUsuario rol, AgendarVisitaRequest request) {

        if (rol != RolUsuario.CLIENTE)
            throw new AccesoNoAutorizadoException("Acceso no autorizado");

        Lead lead = buscarLeadPorId(leadId);

        if (!lead.getClienteId().equals(solicitanteId))
            throw new AccesoNoAutorizadoException("No tienes permiso sobre este lead");

        if (ESTADOS_TERMINALES.contains(lead.getEstado()))
            throw new EstadoTerminalException("El lead ya fue " + lead.getEstado());

        if (lead.getAgenteId() == null)
            throw new SinAgentesDisponiblesException("Este lead no tiene un agente asignado");

        PropiedadCoordenadasResponse propiedad = propiedadClientService.buscarCoordenadas(lead.getPropiedadId());

        if (propiedad == null || propiedad.estadoComercial() != EstadoComercial.DISPONIBLE)
            throw new PropiedadNoEncontradaException("La propiedad ya no está disponible");

        VisitaReservadaResponse visita = visitaClientService.reservar(new ReservarVisitaRequest(
                leadId, lead.getPropiedadId(), lead.getClienteId(), lead.getAgenteId(), request.fechaHora()));

        if (lead.getEstado() == Estado.CONTACTADO) {
            try {
                lead.setEstado(Estado.VISITA_AGENDADA);
                lead = leadRepository.save(lead);
            } catch (RuntimeException e) {
                visitaClientService.cancelarPorLead(leadId);
                throw e;
            }
        }

        return LeadResponse.from(lead, visita);
    }

    public void cambiarEstado(UUID leadId, Estado nuevoEstado, RolUsuario rolSolicitante, UUID solicitanteId) {
        Lead leadPorId = buscarLeadPorId(leadId);

        boolean esDueno = leadPorId.getAgenteId() != null && leadPorId.getAgenteId().equals(solicitanteId);
        boolean esAdmin = rolSolicitante == RolUsuario.ADMINISTRADOR_CENTRAL;

        if (!esAdmin && !esDueno)
            throw new AccesoNoAutorizadoException("Acceso no autorizado");

        if (leadPorId.getEstado() == Estado.CERRADO || leadPorId.getEstado() == Estado.DESCARTADO)
            throw new EstadoTerminalException("El lead ya fue " + leadPorId.getEstado());

        leadPorId.setEstado(nuevoEstado);
        leadRepository.save(leadPorId);

        if (nuevoEstado == Estado.CERRADO)
            log.info("lead.setEstado(nuevoEstado)");
    }

    public List<LeadResponse> listarMisLeads(UUID idAgente, Estado estadoFiltrado) {

        if (estadoFiltrado != null)
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


    private Lead buscarLeadPorId(UUID id) {
        return leadRepository.findById(id).orElseThrow(() ->
                new LeadNoEncontradoException("Lead no encontrado")
        );
    }

    private Candidatos buscarCandidatos(double latPropiedad, double lonPropiedad) {
        List<OficinaResponse> oficinas = usuarioClientService.listarOficinas();

        Map<UUID, Double> distanciaPorOficina = oficinas.stream()
                .collect(Collectors.toMap(
                        OficinaResponse::getId,
                        o -> GeoUtils.calcularDistancia(latPropiedad, lonPropiedad, o.getLatitud(), o.getLongitud())
                ));

        List<UUID> oficinasIdsCercanas = distanciaPorOficina.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(N_OFICINAS_CERCANAS)
                .map(Map.Entry::getKey)
                .toList();

        List<AgenteInternalResponse> candidatos = agenteClientService.listarAgentesInterno(oficinasIdsCercanas);

        if (candidatos.isEmpty())
            throw new SinAgentesDisponiblesException("No hay agentes disponibles para asignar");

        return new Candidatos(candidatos, distanciaPorOficina);
    }

    private List<AgenteInternalResponse> filtrarLibres(List<AgenteInternalResponse> candidatos, LocalDateTime fechaHora) {
        List<UUID> ids = candidatos.stream().map(AgenteInternalResponse::usuarioId).toList();

        Set<UUID> ocupados = Set.copyOf(visitaClientService.consultarAgentesOcupados(ids, fechaHora));

        List<AgenteInternalResponse> libres = candidatos.stream()
                .filter(a -> !ocupados.contains(a.usuarioId()))
                .toList();

        if (libres.isEmpty())
            throw new SinAgentesDisponiblesException("No hay agentes disponibles en ese horario, elige otra hora");

        return libres;
    }


    private List<AgenteInternalResponse> ordenarPorPuntaje(List<AgenteInternalResponse> agentes,
                                                           Map<UUID, Double> distanciaPorOficina) {
        DistributionRules reglas = distributionRulesRepository.findAll().getFirst();

        Map<UUID, Double> puntajePorAgente = agentes.stream()
                .collect(Collectors.toMap(
                        AgenteInternalResponse::usuarioId,
                        a -> calcularPuntaje(a, distanciaPorOficina, reglas)
                ));

        return agentes.stream()
                .sorted(Comparator.comparingDouble(
                        (AgenteInternalResponse a) -> puntajePorAgente.get(a.usuarioId())).reversed())
                .toList();
    }


    private VisitaReservadaResponse reservarConPrimerAgenteLibre(UUID leadId, UUID clienteId,
                                                                 CrearLeadRequest request,
                                                                 List<AgenteInternalResponse> ranking) {
        for (AgenteInternalResponse agente : ranking) {
            try {
                return visitaClientService.reservar(new ReservarVisitaRequest(
                        leadId, request.propiedadId(), clienteId, agente.usuarioId(), request.fechaHora()));
            } catch (AgenteOcupadoException e) {
                log.info("El agente {} se ocupó antes de reservar, se prueba con el siguiente", agente.usuarioId());
            }
        }
        throw new SinAgentesDisponiblesException("No hay agentes disponibles en ese horario, elige otra hora");
    }

    private Lead guardarCompensando(Lead lead) {
        try {
            return leadRepository.save(lead);
        } catch (DataIntegrityViolationException e) {
            visitaClientService.cancelarPorLead(lead.getId());
            if (esDuplicadoDeLeadActivo(e))
                throw new LeadDuplicadoException("Ya tienes un contacto activo para esta propiedad");
            throw e;
        } catch (RuntimeException e) {
            visitaClientService.cancelarPorLead(lead.getId());
            throw e;
        }
    }

    private double calcularPuntaje(AgenteInternalResponse agente, Map<UUID, Double> distanciaPorOficina, DistributionRules reglas) {
        double distancia = distanciaPorOficina.get(agente.oficinaId());
        double scoreProximidad = 1.0 / (1.0 + distancia);

        long cargaActual = leadRepository.countByAgenteIdAndEstadoNotIn(
                agente.usuarioId(), List.of(Estado.CERRADO, Estado.DESCARTADO));

        double scoreCarga = 1.0 / (1.0 + cargaActual);

        long totalLeads = leadRepository.countByAgenteId(agente.usuarioId());
        long leadsGanados = leadRepository.countByAgenteIdAndEstado(agente.usuarioId(), Estado.CERRADO);
        double scoreConversion = totalLeads == 0 ? 0.5 : (double) leadsGanados / totalLeads;

        return reglas.getPesoZona() * scoreProximidad
                + reglas.getPesoCarga() * scoreCarga
                + reglas.getPesoConversion() * scoreConversion;

    }


    private boolean esDuplicadoDeLeadActivo(DataIntegrityViolationException e) {
        String mensaje = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        return mensaje != null && mensaje.contains(INDICE_LEAD_ACTIVO);
    }


}
