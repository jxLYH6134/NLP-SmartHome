package link.crychic.smarthome.model;

import link.crychic.smarthome.annotation.InjectUserId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@InjectUserId
public class RuleRequest {
    private Integer ruleId;
    private String description;
    private Boolean isEnabled;
    private String ownerId;
}
