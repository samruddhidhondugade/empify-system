package com.empsys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.empsys.entity.UserCreds;

public interface UserCredsRepository extends JpaRepository<UserCreds, Long> {
    UserCreds findByUsername(String username);
}
