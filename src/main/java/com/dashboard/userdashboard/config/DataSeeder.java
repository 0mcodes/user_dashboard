package com.dashboard.userdashboard.config;

import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedSampleUsers();
    }

    private void seedAdmin() {
        // Check first — don't create duplicates on every restart
        if (userRepository.existsByEmail("admin@dashboard.com")) {
            return;
        }

        User admin = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email("admin@dashboard.com")
                .password(passwordEncoder.encode("Admin@123"))
                .phoneNumber("+910000000000")
                .role(Role.ROLE_ADMIN)
                .bio("System administrator account")
                .build();

        userRepository.save(admin);

        log.info("================================================");
        log.info("  DEFAULT ADMIN ACCOUNT CREATED");
        log.info("  Email:    admin@dashboard.com");
        log.info("  Password: Admin@123");
        log.info("  CHANGE THIS PASSWORD IN PRODUCTION!");
        log.info("================================================");
    }

    private void seedSampleUsers() {
        // Only seed if just the admin exists
        if (userRepository.count() > 1) {
            return;
        }

        User manager = User.builder()
                .firstName("Priya").lastName("Sharma")
                .email("manager@dashboard.com")
                .password(passwordEncoder.encode("Manager@123"))
                .phoneNumber("+911111111111")
                .role(Role.ROLE_MANAGER)
                .bio("Team manager")
                .build();

        User user1 = User.builder()
                .firstName("Rahul").lastName("Verma")
                .email("rahul@dashboard.com")
                .password(passwordEncoder.encode("User@1234"))
                .phoneNumber("+912222222222")
                .role(Role.ROLE_USER)
                .bio("Software developer")
                .build();

        User user2 = User.builder()
                .firstName("Ananya").lastName("Singh")
                .email("ananya@dashboard.com")
                .password(passwordEncoder.encode("User@1234"))
                .phoneNumber("+913333333333")
                .role(Role.ROLE_USER)
                .bio("UI designer")
                .build();

        userRepository.save(manager);
        userRepository.save(user1);
        userRepository.save(user2);

        log.info("Sample users created:");
        log.info("  manager@dashboard.com  / Manager@123");
        log.info("  rahul@dashboard.com    / User@1234");
        log.info("  ananya@dashboard.com   / User@1234");
    }
}
