package org.example.dao;

import org.example.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}