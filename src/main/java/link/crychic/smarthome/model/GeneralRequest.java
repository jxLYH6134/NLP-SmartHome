package link.crychic.smarthome.model;

import link.crychic.smarthome.annotation.InjectUserId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@InjectUserId
public class GeneralRequest {
    private String ownerId;
    private String type;
    private String params;
    private String deviceId;
    private String deviceName;
    private Integer roomId;
    private String roomName;
    private Integer familyGroupId;
    private String groupName;
}
