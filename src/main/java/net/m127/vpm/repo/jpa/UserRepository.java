package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String username);
    boolean existsByName(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String vrc_id);
    Optional<User> findByVRCId(String vrc_id);
    boolean existsByVRCId(String vrc_id);
    List<User> findByAdmin(boolean isAdmin);
    int countByApprover(User user);
    List<User> findByApprover(User user);
}
