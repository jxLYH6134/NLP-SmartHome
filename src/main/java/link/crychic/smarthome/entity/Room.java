package link.crychic.smarthome.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    private String roomId;
    private String roomName;
    private String ownerId;
    private String familyGroupId;
}
