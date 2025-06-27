package link.crychic.smarthome.repository;

import link.crychic.smarthome.entity.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, String> {
    FamilyGroup findByOwnerId(String ownerId);
}
