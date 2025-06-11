package link.crychic.smarthome.repository;

import link.crychic.smarthome.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByOwnerId(String ownerId);

    List<Device> findByRoomId(Integer roomId);
}