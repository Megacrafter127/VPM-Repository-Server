package net.m127.vpm.repo.jpa;

import net.m127.vpm.repo.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
