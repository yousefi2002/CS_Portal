package com.manus.digitalecosystem.service;

import com.manus.digitalecosystem.dto.request.CreateStudentAccountRequest;
import com.manus.digitalecosystem.dto.request.DeleteStudentRequest;
import com.manus.digitalecosystem.dto.response.StudentResponse;
import com.manus.digitalecosystem.exception.ResourceNotFoundException;
import com.manus.digitalecosystem.model.Department;
import com.manus.digitalecosystem.model.Student;
import com.manus.digitalecosystem.model.University;
import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.repository.DepartmentRepository;
import com.manus.digitalecosystem.repository.StudentRepository;
import com.manus.digitalecosystem.repository.UniversityRepository;
import com.manus.digitalecosystem.repository.UserRepository;
import com.manus.digitalecosystem.security.UserDetailsImpl;
import com.manus.digitalecosystem.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        studentService = new StudentServiceImpl(
                studentRepository,
                userRepository,
                universityRepository,
                departmentRepository,
                passwordEncoder,
                "uploads",
                "http://localhost:8080"
        );
    }

    @Test
    void createStudentRollsBackUserWhenStudentCreationFails() {
        authenticate(Role.SUPER_ADMIN, "super-1");

        CreateStudentAccountRequest request = new CreateStudentAccountRequest();
        request.setFullName("Test Student");
        request.setEmail("s@test.com");
        request.setPassword("123456");
        request.setUniversityId("u-1");
        request.setDepartmentId("d-1");

        when(userRepository.existsByEmail("s@test.com")).thenReturn(false);
        when(departmentRepository.findById("d-1")).thenReturn(Optional.of(Department.builder().id("d-1").universityId("u-1").deleted(false).build()));
        when(universityRepository.findById("u-1")).thenReturn(Optional.of(University.builder().id("u-1").build()));
        when(passwordEncoder.encode("123456")).thenReturn("hashed");

        User savedUser = User.builder().id("user-1").email("s@test.com").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(studentRepository.save(any(Student.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class, () -> studentService.createStudent(request));
        verify(userRepository).delete(savedUser);
    }

    @Test
    void getStudentByIdHidesDeletedForNonSuperAdmin() {
        authenticate(Role.STUDENT, "user-1");
        when(studentRepository.findById("s-1")).thenReturn(Optional.of(Student.builder().id("s-1").deleted(true).build()));

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById("s-1"));
    }

    @Test
    void softDeleteBySelfSuspendsUser() {
        authenticate(Role.STUDENT, "user-1");

        Student student = Student.builder().id("s-1").userId("user-1").deleted(false).build();
        User user = User.builder().id("user-1").status(Status.ACTIVE).build();

        when(studentRepository.findById("s-1")).thenReturn(Optional.of(student));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeleteStudentRequest request = new DeleteStudentRequest();
        request.setDeleted(true);

        StudentResponse response = studentService.softDeleteStudent("s-1", request);

        assertTrue(response.isDeleted());
        assertEquals(Status.SUSPENDED, user.getStatus());
    }

    private void authenticate(Role role, String userId) {
        UserDetailsImpl principal = new UserDetailsImpl(
                userId,
                userId + "@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())),
                true
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
