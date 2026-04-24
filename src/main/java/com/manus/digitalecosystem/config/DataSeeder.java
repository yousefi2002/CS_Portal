package com.manus.digitalecosystem.config;

import com.manus.digitalecosystem.model.User;
import com.manus.digitalecosystem.model.enums.Role;
import com.manus.digitalecosystem.model.enums.Status;
import com.manus.digitalecosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            User admin = User.builder()
                .fullName("System Administrator")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.SUPER_ADMIN)
                    .status(Status.ACTIVE)
                    .build();
            userRepository.save(admin);
            System.out.println("Initial Super Admin created: admin@gmail.com / admin123");
        }
    }
}
