package link.crychic.smarthome.model;

import link.crychic.smarthome.annotation.InjectUserId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@InjectUserId
public class GeneralRequest {
    private String ownerId;
    private String deviceId;
    private Integer roomId;
    private Integer familyGroupId;
    private String deviceName;
    private String roomName;
    private String groupName;
}
