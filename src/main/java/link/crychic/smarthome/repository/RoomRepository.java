package link.crychic.smarthome.repository;

import link.crychic.smarthome.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByFamilyGroupId(Integer familyGroupId);

    List<Room> findByOwnerId(String ownerId);
}
