package link.crychic.smarthome.controller;

import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.model.RuleRequest;
import link.crychic.smarthome.service.AutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {
    @Autowired
    private AutomationService automationService;

    @PostMapping("/create")
    public ApiResponse createRule(@RequestBody RuleRequest request) {
        return automationService.createRule(
                request.getDescription(),
                request.getOwnerId()
        );
    }

    @PutMapping("/toggle")
    public ApiResponse toggleRuleStatus(@RequestBody RuleRequest request) {
        return automationService.toggleRuleStatus(
                request.getRuleId(),
                request.getOwnerId()
        );
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteRule(@RequestBody RuleRequest request) {
        return automationService.deleteRule(request.getRuleId(), request.getOwnerId());
    }

    @GetMapping("/list")
    public ApiResponse getUserRules(@RequestHeader("X-User-Id") String userId) {
        return automationService.getUserRules(userId);
    }
}
