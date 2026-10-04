package service.leads.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.leads.entity.Estado;
import service.leads.entity.Lead;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    long countByAgenteIdAndEstadoNotIn(UUID agenteId, List<Estado> estados);
    long countByAgenteId(UUID agenteId);
    long countByAgenteIdAndEstado(UUID agenteId, Estado estado);
    boolean existsByClienteIdAndPropiedadIdAndEstadoNotIn(UUID clienteId, UUID propiedadId, List<Estado> estados);

    List<Lead> findByAgenteId(UUID agenteId);
    List<Lead> findByAgenteIdAndEstado(UUID agenteId, Estado estado);

}
