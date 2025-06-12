package link.crychic.smarthome.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class DeviceRequest {
    private String deviceId;
    private String deviceName;
    private String type;
    private Timestamp lastHeartbeat;
    private JsonNode params;
}
