package link.crychic.smarthome.repository;

import link.crychic.smarthome.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
