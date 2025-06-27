package link.crychic.smarthome.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MqttCommand {
    private String command;
    private String deviceId;
    private String type;
    private JsonNode params;
}
