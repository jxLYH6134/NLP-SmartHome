package link.crychic.smarthome.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "family_groups")
public class FamilyGroup {
    @Id
    private Integer familyGroupId;
    private String groupName;
    private String ownerId;
}
