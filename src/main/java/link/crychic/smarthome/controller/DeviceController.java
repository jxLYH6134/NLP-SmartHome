package link.crychic.smarthome.controller;

import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.model.GeneralRequest;
import link.crychic.smarthome.model.MqttCommand;
import link.crychic.smarthome.service.DeviceService;
import link.crychic.smarthome.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private MqttService mqttService;

    @GetMapping("/get")
    public ApiResponse getDevice(
            @RequestParam("deviceId") String deviceId) {
        return deviceService.getDevice(deviceId);
    }

    @PostMapping("/control")
    public ApiResponse sendControlCommand(@RequestBody MqttCommand request) {
        try {
            if (request.getDeviceId() == null) {
                return ApiResponse.error(2, "参数错误: 缺少deviceId");
            }

            if (request.getParams() == null) {
                return ApiResponse.error(2, "参数错误: 缺少控制参数");
            }

            mqttService.sendControlMessage(request.getDeviceId(), request.getParams());
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "发送控制命令失败");
        }
    }

    @PutMapping("/update")
    public ApiResponse updateDevice(@RequestBody GeneralRequest request) {
        return deviceService.updateDevice(
                request.getDeviceId(),
                request.getDeviceName(),
                request.getOwnerId(),
                request.getRoomId());
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteDevice(@RequestBody GeneralRequest request) {
        return deviceService.deleteDevice(request.getDeviceId(), request.getOwnerId());
    }

    @GetMapping("/list/room")
    public ApiResponse getRoomDevices(@RequestParam("roomId") Integer roomId) {
        return deviceService.getRoomDevices(roomId);
    }

    @GetMapping("/list/user")
    public ApiResponse getUserDevices(@RequestHeader("X-User-Id") String ownerId) {
        return deviceService.getUserDevices(ownerId);
    }
}
