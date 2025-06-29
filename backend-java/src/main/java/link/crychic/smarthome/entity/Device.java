package link.crychic.smarthome.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "devices")
public class Device {
    @Id
    private String deviceId;
    private String deviceName;
    private String type;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private JsonNode params;
    @Column(name = "last_heartbeat", columnDefinition = "TIMESTAMP NULL DEFAULT NULL")
    private Timestamp lastHeartbeat;
    private String ownerId;
    private Integer roomId;
}
