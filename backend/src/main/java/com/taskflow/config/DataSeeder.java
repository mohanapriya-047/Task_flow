package com.taskflow.config;

import com.taskflow.entity.User;
import com.taskflow.enums.Role;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps exactly one ADMIN account from environment variables, ONLY if
 * no admin exists yet and all four bootstrap env vars are set. This is the
 * only supported way to create the first Admin - never through the public
 * registration endpoint (see AuthService.register()).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${taskflow.admin.bootstrap-username}")
    private String bootstrapUsername;
    @Value("${taskflow.admin.bootstrap-password}")
    private String bootstrapPassword;
    @Value("${taskflow.admin.bootstrap-email}")
    private String bootstrapEmail;
    @Value("${taskflow.admin.bootstrap-phone}")
    private String bootstrapPhone;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return; // an admin already exists - do nothing
        }
        if (isBlank(bootstrapUsername) || isBlank(bootstrapPassword)
                || isBlank(bootstrapEmail) || isBlank(bootstrapPhone)) {
            log.warn("No ADMIN account exists yet and ADMIN_BOOTSTRAP_* env vars are not fully set. "
                    + "Set ADMIN_BOOTSTRAP_USERNAME/PASSWORD/EMAIL/PHONE and restart to create the first admin.");
            return;
        }

        User admin = User.builder()
                .name("System Administrator")
                .username(bootstrapUsername)
                .email(bootstrapEmail)
                .passwordHash(passwordEncoder.encode(bootstrapPassword))
                .phoneNumber(bootstrapPhone)
                .role(Role.ADMIN)
                .emailVerified(true)
                .phoneVerified(true)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.info("Bootstrap ADMIN account '{}' created.", bootstrapUsername);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
