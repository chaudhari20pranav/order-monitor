package com.ordermonitor.service;

import com.ordermonitor.dto.RegisterRequest;
import com.ordermonitor.entity.User;
import com.ordermonitor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("register: should hash password and save user")
    void register_shouldHashPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Alice Trader");
        req.setEmail("alice@example.com");
        req.setPassword("secret123");
        req.setRole("SUBSCRIBER");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.register(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        // Password must be BCrypt-encoded, not plaintext
        assertThat(result.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", result.getPassword())).isTrue();
        assertThat(result.getRole()).isEqualTo("SUBSCRIBER");
    }

    @Test
    @DisplayName("register: should throw when email already exists")
    void register_shouldThrowOnDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("duplicate@example.com");
        req.setPassword("pass");
        req.setRole("SUBSCRIBER");
        req.setFullName("Dup User");

        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("updateLastActive: should update timestamp")
    void updateLastActive_shouldPersistTimestamp() {
        User user = User.builder()
                .id(5L)
                .fullName("Bob")
                .email("bob@example.com")
                .password("hashed")
                .role("ADMIN")
                .lastActive(LocalDateTime.now().minusHours(10))
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateLastActive(5L);

        verify(userRepository).save(argThat(u ->
                u.getLastActive().isAfter(LocalDateTime.now().minusSeconds(5))
        ));
    }
}
