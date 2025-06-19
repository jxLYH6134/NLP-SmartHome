package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.FamilyGroup;
import link.crychic.smarthome.entity.Room;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.FamilyGroupRepository;
import link.crychic.smarthome.repository.RoomRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FamilyGroupService {
    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiResponse getFamilyGroup(String familyGroupId) {
        try {
            if (familyGroupId == null) {
                return ApiResponse.error(2, "参数错误: 缺少familyGroupId");
            }

            FamilyGroup familyGroup = familyGroupRepository.findById(familyGroupId).orElse(null);
            if (familyGroup == null) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            return ApiResponse.success(familyGroup);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse createFamilyGroup(String groupName, String ownerId) {
        try {
            if (groupName == null) {
                return ApiResponse.error(2, "参数错误: 缺少groupName");
            }

            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }

            FamilyGroup familyGroup = new FamilyGroup();
            familyGroup.setFamilyGroupId(UUID.randomUUID().toString());
            familyGroup.setGroupName(groupName);
            familyGroup.setOwnerId(ownerId);

            familyGroupRepository.save(familyGroup);

            return ApiResponse.success(familyGroup.getFamilyGroupId());
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse updateFamilyGroup(String familyGroupId, String groupName, String ownerId) {
        try {
            if (familyGroupId == null) {
                return ApiResponse.error(2, "参数错误: 缺少familyGroupId");
            }

            if (groupName == null) {
                return ApiResponse.error(2, "参数错误: 缺少groupName");
            }

            FamilyGroup familyGroup = familyGroupRepository.findById(familyGroupId).orElse(null);
            if (familyGroup == null) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            if (!familyGroup.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限修改此家庭组");
            }

            familyGroup.setGroupName(groupName);
            familyGroupRepository.save(familyGroup);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse deleteFamilyGroup(String familyGroupId, String ownerId) {
        try {
            if (familyGroupId == null) {
                return ApiResponse.error(2, "参数错误: 缺少familyGroupId");
            }

            FamilyGroup familyGroup = familyGroupRepository.findById(familyGroupId).orElse(null);
            if (familyGroup == null) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            if (!familyGroup.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限删除此家庭组");
            }

            // 释放所属房间，将familyGroupId设为null
            List<Room> rooms = roomRepository.findByFamilyGroupId(familyGroupId);
            for (Room room : rooms) {
                room.setFamilyGroupId(null);
                roomRepository.save(room);
            }

            familyGroupRepository.deleteById(familyGroupId);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getUserFamilyGroups(String ownerId) {
        try {
            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }

            List<FamilyGroup> familyGroups = familyGroupRepository.findAll()
                    .stream()
                    .filter(group -> group.getOwnerId().equals(ownerId))
                    .toList();

            return ApiResponse.success(familyGroups);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }
}
