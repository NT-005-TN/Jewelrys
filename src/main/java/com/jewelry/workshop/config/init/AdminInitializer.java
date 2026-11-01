package com.jewelry.workshop.config.init;

import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(prefix = "app.init.admin", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.username:}")
    private String username;

    @Value("${app.init.admin.email:}")
    private String email;

    @Value("${app.init.admin.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateConfiguration();

        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        String normalizedUsername = username.trim();
        if (userRepository.existsByUsername(normalizedUsername) || userRepository.existsByEmail(normalizedEmail)) {
            log.info("Первичный администратор уже существует; инициализация пропущена");
            return;
        }

        User admin = new User();
        admin.setUsername(normalizedUsername);
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);

        userRepository.save(admin);
        log.warn("Первичный администратор создан. Отключите ADMIN_INIT_ENABLED и смените пароль после входа");
    }

    private void validateConfiguration() {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("ADMIN_USERNAME обязателен при ADMIN_INIT_ENABLED=true");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalStateException("Корректный ADMIN_EMAIL обязателен при ADMIN_INIT_ENABLED=true");
        }
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw new IllegalStateException("ADMIN_PASSWORD должен содержать от 12 до 128 символов");
        }
    }
}
