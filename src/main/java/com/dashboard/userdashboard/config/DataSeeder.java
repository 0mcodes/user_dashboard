package com.dashboard.userdashboard.config;

import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * @Value reads from application.properties (or the active profile).
     * The colon : provides a default value if the property is not set.
     * Format: @Value("${property.name:defaultValue}")
     *
     * In production (Railway/Render), you set these as
     * environment variables and they override the defaults.
     * This way credentials are never hardcoded in the source code.
     */
    @Value("${app.admin.email:admin@dashboard.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.firstName:System}")
    private String adminFirstName;

    @Value("${app.admin.lastName:Admin}")
    private String adminLastName;

    @Value("${app.seed.sample-users:true}")
    private boolean seedSampleUsers;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        if (seedSampleUsers) {
            seedSampleUsersData();
        }
    }

    private void seedAdmin() {
        /*
         * existsByEmail checks if a user with this email already exists.
         * This makes the seeder IDEMPOTENT — running it multiple times
         * produces the same result as running it once.
         * On every app restart with MySQL (persistent data),
         * the admin already exists, so we skip creation.
         * On first ever start with empty database, we create the admin.
         */
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists — skipping seed");
            return;
        }

        User admin = User.builder()
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phoneNumber("+910000000000")
                .role(Role.ROLE_ADMIN)
                .bio("System administrator account")
                .build();

        userRepository.save(admin);

        log.info("╔══════════════════════════════════════════╗");
        log.info("║     DEFAULT ADMIN ACCOUNT CREATED        ║");
        log.info("║  Email:    {}  ║", adminEmail);
        log.info("║  Password: {}               ║", adminPassword);
        log.info("║  CHANGE THIS PASSWORD IN PRODUCTION!     ║");
        log.info("╚══════════════════════════════════════════╝");
    }

    private void seedSampleUsersData() {
        /*
         * count() returns the total number of rows in the users table.
         * If count > 1, sample users were already seeded in a
         * previous run (the admin is always row 1).
         * We skip to avoid duplicates.
         */
        if (userRepository.count() > 1) {
            log.info("Sample users already exist — skipping seed");
            return;
        }

        User manager = User.builder()
                .firstName("Priya")
                .lastName("Sharma")
                .email("manager@dashboard.com")
                .password(passwordEncoder.encode("Manager@123"))
                .phoneNumber("+911111111111")
                .role(Role.ROLE_MANAGER)
                .bio("Team manager")
                .location("Mumbai, India")
                .build();

        User user1 = User.builder()
                .firstName("Rahul")
                .lastName("Verma")
                .email("rahul@dashboard.com")
                .password(passwordEncoder.encode("User@1234"))
                .phoneNumber("+912222222222")
                .role(Role.ROLE_USER)
                .bio("Software developer")
                .location("Bangalore, India")
                .build();

        User user2 = User.builder()
                .firstName("Ananya")
                .lastName("Singh")
                .email("ananya@dashboard.com")
                .password(passwordEncoder.encode("User@1234"))
                .phoneNumber("+913333333333")
                .role(Role.ROLE_USER)
                .bio("UI designer")
                .location("Hyderabad, India")
                .build();

        userRepository.save(manager);
        userRepository.save(user1);
        userRepository.save(user2);

        log.info("Sample users seeded successfully:");
        log.info("  manager@dashboard.com / Manager@123");
        log.info("  rahul@dashboard.com   / User@1234");
        log.info("  ananya@dashboard.com  / User@1234");
    }
}