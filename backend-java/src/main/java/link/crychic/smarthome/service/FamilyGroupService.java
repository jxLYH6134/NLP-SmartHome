package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.FamilyGroup;
import link.crychic.smarthome.entity.Room;
import link.crychic.smarthome.entity.User;
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

    public ApiResponse getFamilyGroup(String ownerId) {
        try {
            User user = userRepository.findById(ownerId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            if (user.getFamilyGroupId() == null) {
                return ApiResponse.success();
            }

            FamilyGroup familyGroup = familyGroupRepository.findById(user.getFamilyGroupId()).orElse(null);
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

            User user = userRepository.findById(ownerId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            if (user.getFamilyGroupId() != null) {
                return ApiResponse.error(7, "用户已在其他家庭组中，请先退出当前家庭组");
            }

            FamilyGroup familyGroup = new FamilyGroup();
            familyGroup.setFamilyGroupId(UUID.randomUUID().toString());
            familyGroup.setGroupName(groupName);
            familyGroup.setOwnerId(ownerId);

            familyGroupRepository.save(familyGroup);

            user.setFamilyGroupId(familyGroup.getFamilyGroupId());
            userRepository.save(user);

            return ApiResponse.success(familyGroup.getFamilyGroupId());
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse updateFamilyGroup(String groupName, String ownerId) {
        try {
            if (groupName == null) {
                return ApiResponse.error(2, "参数错误: 缺少groupName");
            }

            User user = userRepository.findById(ownerId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            if (user.getFamilyGroupId() == null) {
                return ApiResponse.error(7, "没有可以修改的家庭组");
            }

            FamilyGroup familyGroup = familyGroupRepository.findById(user.getFamilyGroupId()).orElse(null);
            if (familyGroup == null) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            familyGroup.setGroupName(groupName);
            familyGroupRepository.save(familyGroup);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse deleteFamilyGroup(String ownerId) {
        try {
            User user = userRepository.findById(ownerId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }
            String familyGroupId = user.getFamilyGroupId();

            if (familyGroupId == null) {
                return ApiResponse.error(7, "没有可以删除的家庭组");
            }

            FamilyGroup familyGroup = familyGroupRepository.findById(familyGroupId).orElse(null);
            if (familyGroup == null) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            user.setFamilyGroupId(null);

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

    public ApiResponse joinFamilyGroup(String familyGroupId, String userId) {
        try {
            if (familyGroupId == null) {
                return ApiResponse.error(2, "参数错误: 缺少familyGroupId");
            }

            if (!familyGroupRepository.existsById(familyGroupId)) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            user.setFamilyGroupId(familyGroupId);
            userRepository.save(user);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse leaveFamilyGroup(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            FamilyGroup familyGroup = familyGroupRepository.findByOwnerId(userId);
            if (familyGroup != null && familyGroup.getOwnerId().equals(userId)) {
                return ApiResponse.error(7, "组长不能退出家庭组");
            }

            user.setFamilyGroupId(null);
            userRepository.save(user);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }
}
