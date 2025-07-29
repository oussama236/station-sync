package tn.spring.stationsync.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.stationsync.Entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String token);
    boolean existsByUsername(String username);
}