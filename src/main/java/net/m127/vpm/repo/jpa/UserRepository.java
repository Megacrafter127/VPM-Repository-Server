package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String username);
    boolean existsByName(String username);
    User findByEmail(String email);
    boolean existsByEmail(String vrc_id);
    User findByVRCId(String vrc_id);
    boolean existsByVRCId(String vrc_id);
}
