package link.crychic.smarthome.controller;

import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.model.GeneralRequest;
import link.crychic.smarthome.service.FamilyGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyGroupController {
    @Autowired
    private FamilyGroupService familyGroupService;

    @GetMapping("/get")
    public ApiResponse getFamilyGroup(
            @RequestParam("familyGroupId") Integer familyGroupId) {
        return familyGroupService.getFamilyGroup(familyGroupId);
    }

    @PostMapping("/create")
    public ApiResponse createFamilyGroup(@RequestBody GeneralRequest request) {
        return familyGroupService.createFamilyGroup(request.getGroupName(), request.getOwnerId());
    }

    @PutMapping("/update")
    public ApiResponse updateFamilyGroup(@RequestBody GeneralRequest request) {
        return familyGroupService.updateFamilyGroup(request.getFamilyGroupId(), request.getGroupName(), request.getOwnerId());
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteFamilyGroup(@RequestBody GeneralRequest request) {
        return familyGroupService.deleteFamilyGroup(request.getFamilyGroupId(), request.getOwnerId());
    }

    @GetMapping("/list")
    public ApiResponse getUserFamilyGroups(@RequestHeader("X-User-Id") String userId) {
        return familyGroupService.getUserFamilyGroups(userId);
    }
}
