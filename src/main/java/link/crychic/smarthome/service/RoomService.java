package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.Device;
import link.crychic.smarthome.entity.FamilyGroup;
import link.crychic.smarthome.entity.Room;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.DeviceRepository;
import link.crychic.smarthome.repository.FamilyGroupRepository;
import link.crychic.smarthome.repository.RoomRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public ApiResponse getFamilyGroupRooms(String familyGroupId) {
        try {
            if (familyGroupId == null) {
                return ApiResponse.error(2, "参数错误: 缺少familyGroupId");
            }

            if (!familyGroupRepository.existsById(familyGroupId)) {
                return ApiResponse.error(7, "家庭组不存在");
            }

            List<Room> rooms = roomRepository.findByFamilyGroupId(familyGroupId);

            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getUserRooms(String ownerId) {
        try {
            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }

            List<Room> rooms = roomRepository.findByOwnerId(ownerId);

            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }
}
