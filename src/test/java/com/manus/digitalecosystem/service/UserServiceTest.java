package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUserRequest;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private User user;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setRole(Role.STUDENT);

        user = User.builder()
                .id("1")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .status(User.Status.ACTIVE)
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(createUserRequest);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser(createUserRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
