package link.crychic.smarthome.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import link.crychic.smarthome.constant.ApiConstants;
import link.crychic.smarthome.entity.Device;
import link.crychic.smarthome.repository.DeviceRepository;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private static final String STATUS_TOPIC = "/status";
    private static final String CONTROL_TOPIC = "/control";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        try {
            // 设置回调处理器
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
                    logger.warn("MQTT连接断开: {}", mqttDisconnectResponse.getReasonString());
                }

                @Override
                public void mqttErrorOccurred(MqttException e) {
                    logger.error("MQTT发生错误", e);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    handleMessage(new String(mqttMessage.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttToken iMqttToken) {
                    logger.debug("消息发送完成: {}", iMqttToken.getMessageId());
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        logger.info("MQTT重新连接成功: {}", serverURI);
                    } else {
                        logger.info("MQTT初始连接成功: {}", serverURI);
                    }
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {
                    logger.debug("收到认证包: reasonCode={}", reasonCode);
                }
            });

            // 订阅状态主题
            mqttClient.subscribe(STATUS_TOPIC, 1);
            logger.info("成功订阅MQTT主题: {}", STATUS_TOPIC);

        } catch (MqttException e) {
            logger.error("MQTT初始化失败", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            logger.error("MQTT清理失败", e);
        }
    }

    private void handleMessage(String payload) {
        try {
            link.crychic.smarthome.model.MqttMessage mqttMessage = objectMapper.readValue(payload, link.crychic.smarthome.model.MqttMessage.class);

            if ("status".equals(mqttMessage.getCommand())) {
                handleStatusUpdate(mqttMessage);
            } else if ("warning".equals(mqttMessage.getCommand())) {
                handleWarning(mqttMessage);
            }

        } catch (Exception e) {
            logger.error("处理MQTT消息失败: {}", payload, e);
        }
    }

    private void handleStatusUpdate(link.crychic.smarthome.model.MqttMessage mqttMessage) {
        try {
            Device device = deviceRepository.findById(mqttMessage.getDeviceId()).orElse(null);

            if (device == null) {
                device = new Device();
                device.setDeviceId(mqttMessage.getDeviceId());
                device.setType(mqttMessage.getType());
                device.setDeviceName(mqttMessage.getType() + "_" + mqttMessage.getDeviceId().substring(0, 4));
            }

            // 更新设备参数
            device.setParams(mqttMessage.getParams());
            deviceRepository.save(device);
        } catch (Exception e) {
            logger.error("更新设备状态失败: {}", mqttMessage.getDeviceId(), e);
        }
    }

    private void handleWarning(link.crychic.smarthome.model.MqttMessage mqttMessage) {
        try {
            String warningMessage = mqttMessage.getParams().asText();
            String notificationUrl = String.format("%s/%s/%s",
                    ApiConstants.NOTIFICATION_API_ENDPOINT,
                    "提示",
                    warningMessage);

            restTemplate.getForEntity(notificationUrl, String.class);
            logger.info("警告通知发送成功: {}", warningMessage);

        } catch (Exception e) {
            logger.error("发送警告通知失败", e);
        }
    }

    public void sendControlMessage(String deviceId, JsonNode params) {
        try {
            link.crychic.smarthome.model.MqttMessage controlMessage = new link.crychic.smarthome.model.MqttMessage();
            controlMessage.setCommand("control");
            controlMessage.setDeviceId(deviceId);
            controlMessage.setParams(params);

            String payload = objectMapper.writeValueAsString(controlMessage);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);

            mqttClient.publish(CONTROL_TOPIC, message);
            logger.info("控制消息发送成功 - 设备: {}, 参数: {}", deviceId, params);

        } catch (Exception e) {
            logger.error("发送控制消息失败 - 设备: {}", deviceId, e);
            throw new RuntimeException("发送MQTT控制消息失败", e);
        }
    }
}