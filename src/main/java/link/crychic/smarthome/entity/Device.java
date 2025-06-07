package link.crychic.smarthome.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "devices")
public class Device {
    @Id
    private String deviceId;
    private String deviceName;
    private String ownerId;
    private String roomId;
}

