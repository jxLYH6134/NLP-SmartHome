package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.Device;
import link.crychic.smarthome.entity.Room;
import link.crychic.smarthome.entity.User;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.model.DeviceRequest;
import link.crychic.smarthome.repository.DeviceRepository;
import link.crychic.smarthome.repository.RoomRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private DeviceRequest createFilteredDevice(Device device) {
        DeviceRequest filteredDevice = new DeviceRequest();
        filteredDevice.setDeviceId(device.getDeviceId());
        filteredDevice.setDeviceName(device.getDeviceName());
        filteredDevice.setType(device.getType());
        filteredDevice.setParams(device.getParams());
        filteredDevice.setLastHeartbeat(device.getLastHeartbeat());
        // DeviceRequest类型不包含ownerId和roomId字段
        return filteredDevice;
    }

    public ApiResponse getDevice(String deviceId, String ownerId) {
        try {
            if (deviceId == null) {
                return ApiResponse.error(2, "参数错误: 缺少deviceId");
            }

            Device device = deviceRepository.findById(deviceId).orElse(null);
            if (device == null) {
                return ApiResponse.error(5, "设备不存在");
            }

            if (!Objects.equals(device.getOwnerId(), ownerId)) {
                // 如果设备没有分配到房间，则无法通过家庭组权限访问
                if (device.getRoomId() == null) {
                    return ApiResponse.error(4, "无权限查看此设备");
                }

                // 检查用户是否在家庭组中，且设备所在房间被共享到该家庭组
                User user = userRepository.findById(ownerId).orElse(null);
                if (user == null || user.getFamilyGroupId() == null) {
                    return ApiResponse.error(4, "无权限查看此设备");
                }

                // 检查设备所在房间是否被共享到用户的家庭组
                Room room = roomRepository.findById(device.getRoomId()).orElse(null);
                if (room == null || !Objects.equals(room.getFamilyGroupId(), user.getFamilyGroupId())) {
                    return ApiResponse.error(4, "无权限查看此设备");
                }
            }

            return ApiResponse.success(createFilteredDevice(device));
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse updateDevice(String deviceId, String deviceName,
                                    String ownerId, Integer roomId) {
        try {
            if (deviceId == null) {
                return ApiResponse.error(2, "参数错误: 缺少deviceId");
            }

            Device device = deviceRepository.findById(deviceId).orElse(null);
            if (device == null) {
                return ApiResponse.error(5, "设备不存在");
            }

            if (device.getOwnerId() != null && !device.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限修改此设备");
            }

            if (deviceName != null) {
                device.setDeviceName(deviceName);
            }

            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }
            device.setOwnerId(ownerId);

            if (roomId != null) {
                if (!roomRepository.existsById(roomId)) {
                    return ApiResponse.error(6, "房间不存在");
                }
            }
            device.setRoomId(roomId);

            deviceRepository.save(device);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse deleteDevice(String deviceId, String ownerId) {
        try {
            Device device = deviceRepository.findById(deviceId).orElse(null);
            if (device == null) {
                return ApiResponse.error(5, "设备不存在");
            }

            if (!device.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限删除此设备");
            }

            deviceRepository.deleteById(deviceId);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getRoomDevices(Integer roomId) {
        try {
            if (!roomRepository.existsById(roomId)) {
                return ApiResponse.error(6, "房间不存在");
            }

            List<Device> devices = deviceRepository.findByRoomId(roomId);

            // 移除敏感字段（ownerId和roomId）
            List<DeviceRequest> filteredDevices = devices.stream()
                    .map(this::createFilteredDevice)
                    .toList();

            return ApiResponse.success(filteredDevices);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getUserDevices(String ownerId) {
        try {
            User user = userRepository.findById(ownerId).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            // 获取用户的全部设备（包括在房间内的设备）
            List<Device> userDevices = deviceRepository.findByOwnerId(ownerId);
            Set<Device> allDevices = new HashSet<>(userDevices);

            // 如果用户属于家庭组，追加家庭组共享房间中的设备
            if (user.getFamilyGroupId() != null) {
                // 获取家庭组中的所有房间
                List<Room> familyRooms = roomRepository.findByFamilyGroupId(user.getFamilyGroupId());

                // 获取这些房间中的所有设备
                for (Room room : familyRooms) {
                    List<Device> roomDevices = deviceRepository.findByRoomId(room.getRoomId());
                    allDevices.addAll(roomDevices); // HashSet自动去重
                }
            }

            // 移除敏感字段（ownerId和roomId）
            List<DeviceRequest> filteredDevices = allDevices.stream()
                    .map(this::createFilteredDevice)
                    .collect(Collectors.toList());

            return ApiResponse.success(filteredDevices);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }
}
