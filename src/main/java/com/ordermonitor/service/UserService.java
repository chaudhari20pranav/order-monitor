package com.ordermonitor.service;

import com.ordermonitor.dto.LoginRequest;
import com.ordermonitor.dto.RegisterRequest;
import com.ordermonitor.entity.User;
import com.ordermonitor.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles user registration, login, logout, and session management.
 * Uses BCrypt for password hashing – no Spring Security filters involved.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    static final String SESSION_USER_ID  = "userId";
    static final String SESSION_USER_ROLE = "userRole";
    static final String SESSION_USER_NAME = "userName";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: id={}, role={}", saved.getId(), saved.getRole());
        return saved;
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    public User login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Store minimal info in session
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_ROLE, user.getRole());
        session.setAttribute(SESSION_USER_NAME, user.getFullName());

        // Update last_active timestamp
        updateLastActive(user.getId());

        log.info("User logged in: id={}, role={}", user.getId(), user.getRole());
        return user;
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------

    public void logout(HttpSession session) {
        session.invalidate();
    }

    // ---------------------------------------------------------------
    // Session helpers
    // ---------------------------------------------------------------

    public Long getSessionUserId(HttpSession session) {
        return (Long) session.getAttribute(SESSION_USER_ID);
    }

    public String getSessionUserRole(HttpSession session) {
        return (String) session.getAttribute(SESSION_USER_ROLE);
    }

    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_USER_ID) != null;
    }

    public boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(getSessionUserRole(session));
    }

    // ---------------------------------------------------------------
    // Update last_active (called on each dashboard load)
    // ---------------------------------------------------------------

    @Transactional
    public void updateLastActive(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setLastActive(LocalDateTime.now());
            userRepository.save(u);
        });
    }
}
