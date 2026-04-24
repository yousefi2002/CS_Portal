package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateUserRequest;
import com.manus.digitalecosystem.dto.response.UserResponse;
import com.manus.digitalecosystem.exception.DuplicateResourceException;
import com.manus.digitalecosystem.model.LocalizedText;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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
        createUserRequest.setFullName(LocalizedText.builder().en("Test User").fa("کاربر تست").ps("ازموینې کارن").build());

        user = User.builder()
                .id("1")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .status(Status.ACTIVE)
                .fullName(createUserRequest.getFullName())
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

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(createUserRequest));

        verify(userRepository, never()).save(any(User.class));
    }
}
