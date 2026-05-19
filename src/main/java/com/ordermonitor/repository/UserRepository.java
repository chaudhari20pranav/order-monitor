package com.ordermonitor.repository;

import com.ordermonitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    /** Find admins who have not been active since the given cutoff time */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.lastActive < :cutoff")
    List<User> findInactiveAdmins(LocalDateTime cutoff);

    long countByRole(String role);
}
