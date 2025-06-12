package link.crychic.smarthome.controller;

import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.model.GeneralRequest;
import link.crychic.smarthome.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/get")
    public ApiResponse getRoom(
            @RequestParam("roomId") Integer roomId) {
        return roomService.getRoom(roomId);
    }

    @PostMapping("/create")
    public ApiResponse createRoom(@RequestBody GeneralRequest request) {
        return roomService.createRoom(
                request.getRoomName(),
                request.getOwnerId(),
                request.getFamilyGroupId());
    }

    @PutMapping("/update")
    public ApiResponse updateRoom(@RequestBody GeneralRequest request) {
        return roomService.updateRoom(
                request.getRoomId(),
                request.getRoomName(),
                request.getOwnerId(),
                request.getFamilyGroupId());
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteRoom(@RequestBody GeneralRequest request) {
        return roomService.deleteRoom(request.getRoomId(), request.getOwnerId());
    }

    @GetMapping("/list/family")
    public ApiResponse getFamilyGroupRooms(
            @RequestParam("familyGroupId") Integer familyGroupId) {
        return roomService.getFamilyGroupRooms(familyGroupId);
    }

    @GetMapping("/list/user")
    public ApiResponse getUserRooms(
            @RequestHeader("X-User-Id") String ownerId) {
        return roomService.getUserRooms(ownerId);
    }
}
