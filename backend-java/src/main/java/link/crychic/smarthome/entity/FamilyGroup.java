package link.crychic.smarthome.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "family_groups")
public class FamilyGroup {
    @Id
    private String familyGroupId;
    private String groupName;
    private String ownerId;
}
