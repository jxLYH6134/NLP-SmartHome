package link.crychic.smarthome.service;

import link.crychic.smarthome.entity.Device;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.DeviceRepository;
import link.crychic.smarthome.repository.RoomRepository;
import link.crychic.smarthome.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiResponse getDevice(String deviceId) {
        try {
            if (deviceId == null) {
                return ApiResponse.error(2, "参数错误: 缺少deviceId");
            }

            Device device = deviceRepository.findById(deviceId).orElse(null);
            if (device == null) {
                return ApiResponse.error(5, "设备不存在");
            }

            return ApiResponse.success(device);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse updateDevice(String deviceId, String deviceName, String type,
                                    String params, String ownerId, Integer roomId) {
        try {
            if (deviceId == null) {
                return ApiResponse.error(2, "参数错误: 缺少deviceId");
            }

            Device device = deviceRepository.findById(deviceId).orElse(null);

            // 如果设备不存在，创建新设备
            if (device == null) {
                if (type == null) {
                    return ApiResponse.error(2, "参数错误: 创建设备时缺少type");
                }

                device = new Device();
                device.setDeviceId(deviceId);
                device.setType(type);
            }

            if (deviceName != null && device.getOwnerId() != null && !device.getOwnerId().equals(ownerId)) {
                return ApiResponse.error(4, "无权限修改此设备名称");
            }

            if (deviceName != null) {
                device.setDeviceName(deviceName);
            }

            if (params != null) {
                device.setParams(params);
            }

            if (ownerId != null) {
                if (!userRepository.existsById(ownerId)) {
                    return ApiResponse.error(3, "用户不存在");
                }
                device.setOwnerId(ownerId);
            }

            if (roomId != null) {
                if (!roomRepository.existsById(roomId)) {
                    return ApiResponse.error(6, "房间不存在");
                }
                device.setRoomId(roomId);
            }

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

            return ApiResponse.success(devices);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }

    public ApiResponse getUserDevices(String ownerId) {
        try {
            if (!userRepository.existsById(ownerId)) {
                return ApiResponse.error(3, "用户不存在");
            }

            List<Device> devices = deviceRepository.findByOwnerId(ownerId);

            return ApiResponse.success(devices);
        } catch (Exception e) {
            return ApiResponse.error(100, "操作失败");
        }
    }
}