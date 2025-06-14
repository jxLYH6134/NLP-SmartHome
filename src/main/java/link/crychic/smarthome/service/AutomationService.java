package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.AutomationRule;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.AutomationRuleRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomationService {
    private static final Logger logger = LoggerFactory.getLogger(AutomationService.class);
    private static final long MIN_CYCLE_INTERVAL = 10000;

    @Autowired
    private AutomationRuleRepository automationRuleRepository;

    @Autowired
    private UserRepository userRepository;

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

    private void buildLLMRequest(AutomationRule node) {

    }
}
