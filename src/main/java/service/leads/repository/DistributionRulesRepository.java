package service.leads.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.leads.entity.DistributionRules;

import java.util.UUID;

public interface DistributionRulesRepository extends JpaRepository<DistributionRules, UUID> {
}
