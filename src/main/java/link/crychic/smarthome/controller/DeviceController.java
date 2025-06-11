package link.crychic.smarthome.controller;

import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.model.GeneralRequest;
import link.crychic.smarthome.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @GetMapping("/get")
    public ApiResponse getDevice(
            @RequestParam("deviceId") String deviceId) {
        return deviceService.getDevice(deviceId);
    }

    @PutMapping("/update")
    public ApiResponse updateDevice(@RequestBody GeneralRequest request) {
        return deviceService.updateDevice(request.getDeviceId(), request.getDeviceName(),
                request.getType(), request.getOwnerId(), request.getRoomId());
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteDevice(@RequestBody GeneralRequest request) {
        return deviceService.deleteDevice(request.getDeviceId(), request.getOwnerId());
    }

    @GetMapping("/list/room")
    public ApiResponse getRoomDevices(
            @RequestParam("roomId") Integer roomId) {
        return deviceService.getRoomDevices(roomId);
    }

    @GetMapping("/list/user")
    public ApiResponse getUserDevices(
            @RequestHeader("X-User-Id") String ownerId) {
        return deviceService.getUserDevices(ownerId);
    }
}