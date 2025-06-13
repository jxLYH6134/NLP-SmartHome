package link.crychic.smarthome.repository;

import link.crychic.smarthome.entity.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Integer> {
    List<AutomationRule> findByOwnerId(String creatorUserId);
}
