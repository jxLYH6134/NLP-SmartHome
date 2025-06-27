package link.crychic.smarthome.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "automation_rules")
public class AutomationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ruleId;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Boolean isEnabled;
    private String ownerId;
}
