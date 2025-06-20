package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.Device;
import link.crychic.smarthome.entity.FamilyGroup;
import link.crychic.smarthome.entity.Room;
import link.crychic.smarthome.entity.User;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.DeviceRepository;
import link.crychic.smarthome.repository.FamilyGroupRepository;
import link.crychic.smarthome.repository.RoomRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    public ApiResponse createRoom(String roomName, String ownerId) {
        try {
            if (roomName == null) {
                return ApiResponse.error(2, "参数错误: 缺少roomName");
            }

            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }

            Room room = new Room();
            room.setRoomName(roomName);
            room.setOwnerId(ownerId);

            roomRepository.save(room);

            return ApiResponse.success(room.getRoomId());
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse updateRoom(Integer roomId, String roomName, String ownerId, String familyGroupId) {
        try {
            if (roomId == null) {
                return ApiResponse.error(2, "参数错误: 缺少roomId");
            }

            if (roomName == null) {
                return ApiResponse.error(2, "参数错误: 缺少roomName");
            }

            if (familyGroupId != null) {
                FamilyGroup familyGroup = familyGroupRepository.findById(familyGroupId).orElse(null);
                if (familyGroup == null) {
                    return ApiResponse.error(7, "家庭组不存在");
                }
            }

            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                return ApiResponse.error(6, "房间不存在");
            }

            if (!room.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限修改此房间");
            }

            room.setRoomName(roomName);
            room.setFamilyGroupId(familyGroupId);
            roomRepository.save(room);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse deleteRoom(Integer roomId, String ownerId) {
        try {
            if (roomId == null) {
                return ApiResponse.error(2, "参数错误: 缺少roomId");
            }

            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                return ApiResponse.error(6, "房间不存在");
            }

            if (!room.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限删除此房间");
            }

            // 释放所有设备，将roomId设为null
            List<Device> devices = deviceRepository.findByRoomId(roomId);
            for (Device device : devices) {
                device.setRoomId(null);
                deviceRepository.save(device);
            }

            roomRepository.deleteById(roomId);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getUserRooms(String ownerId) {
        try {
            User user = userRepository.findById(ownerId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            // 获取用户自己的房间
            List<Room> userRooms = roomRepository.findByOwnerId(ownerId);
            Set<Room> allRooms = new HashSet<>(userRooms);

            // 如果用户属于家庭组，追加家庭组的房间
            if (user.getFamilyGroupId() != null) {
                List<Room> familyRooms = roomRepository.findByFamilyGroupId(user.getFamilyGroupId());
                allRooms.addAll(familyRooms); // HashSet自动去重
            }

            return ApiResponse.success(allRooms);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }
}
