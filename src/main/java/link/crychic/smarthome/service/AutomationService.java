package link.crychic.smarthome.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import link.crychic.smarthome.entity.AutomationRule;
import link.crychic.smarthome.entity.Device;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.AutomationRuleRepository;
import link.crychic.smarthome.repository.DeviceRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AutomationService {
    private static final Logger logger = LoggerFactory.getLogger(AutomationService.class);
    private static final long MIN_CYCLE_INTERVAL = 10000;

    @Autowired
    private AutomationRuleRepository automationRuleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MqttService mqttService;

    @Autowired
    private OllamaService ollamaService;

    private volatile long lastCycleTime = 0;

    public ApiResponse createRule(String description, String ownerId) {
        try {
            if (description == null || description.isEmpty()) {
                return ApiResponse.error(2, "参数错误: 缺少description");
            }

            AutomationRule rule = new AutomationRule();
            rule.setDescription(description);
            rule.setOwnerId(ownerId);
            rule.setIsEnabled(true);

            automationRuleRepository.save(rule);
            return ApiResponse.success(rule.getRuleId());
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse toggleRuleStatus(Integer ruleId, String ownerId) {
        try {
            if (ruleId == null) {
                return ApiResponse.error(2, "参数错误: 缺少ruleId");
            }

            AutomationRule rule = automationRuleRepository.findById(ruleId).orElse(null);
            if (rule == null) {
                return ApiResponse.error(8, "规则不存在");
            }

            if (!rule.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限修改此规则");
            }

            rule.setIsEnabled(!rule.getIsEnabled());
            automationRuleRepository.save(rule);
            return ApiResponse.success(rule.getIsEnabled());
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse deleteRule(Integer ruleId, String ownerId) {
        try {
            if (ruleId == null) {
                return ApiResponse.error(2, "参数错误: 缺少ruleId");
            }

            AutomationRule rule = automationRuleRepository.findById(ruleId).orElse(null);
            if (rule == null) {
                return ApiResponse.error(8, "规则不存在");
            }

            if (!rule.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限删除此规则");
            }

            automationRuleRepository.deleteById(ruleId);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getUserRules(String ownerId) {
        try {
            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }

            List<AutomationRule> rules = automationRuleRepository.findByOwnerId(ownerId);
            return ApiResponse.success(rules);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void processEnabledRules() {
        try {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastCycleTime < MIN_CYCLE_INTERVAL) {
                return;
            }

            List<AutomationRule> allRules = automationRuleRepository.findByIsEnabled(true);
            if (allRules.isEmpty()) {
                return;
            }

            logger.info("开始处理自动化规则循环，规则数量: {}", allRules.size());

            for (AutomationRule rule : allRules) {
                try {
                    buildLLMRequest(rule);
                    Thread.sleep(500);
                } catch (Exception e) {
                    logger.error("处理自动化规则失败: {}", rule.getOwnerId(), e);
                }
            }

            lastCycleTime = currentTime;
        } catch (Exception e) {
            logger.error("自动化规则处理循环失败", e);
        }
    }

    private void buildLLMRequest(AutomationRule node) throws JsonProcessingException {
        List<Device> devices = deviceRepository.findByOwnerId(node.getOwnerId());
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String promptDevices = objectMapper.writeValueAsString(devices);
        String promptTime = "\n当前的HH:mm时间是：" + currentTime;
        String promptDefine = """
                \n系统要求：你是一个IoT智能助手，以上是用户所持有的设备，请根据用户描述决定是否要调整某些参数。
                如果不匹配用户所说的条件或目前已经处于你想要调整的状态，你应返回一个空的params，不要调整任何东西。
                你的返回模板：{"deviceId": 你想控制的设备,"params": 你想修改的参数}
                用户要求：""";
        String promptRule = node.getDescription();
        JsonNode response = ollamaService.generateResponse(promptDevices + promptTime + promptDefine + promptRule);
        mqttService.sendControlMessage(response.get("deviceId").asText(), response.get("params"));
    }
}
