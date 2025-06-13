package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.AutomationRule;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.AutomationRuleRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomationService {
    @Autowired
    private AutomationRuleRepository automationRuleRepository;

    @Autowired
    private UserRepository userRepository;

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
}
