package com.jewelry.workshop.config.init;

import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.UserRepository;
import com.jewelry.workshop.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("init-admin")
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    @Value("${app.init.admin.username}")
    private String username;
    @Value("${app.init.admin.email}")
    private String email;
    @Value("${app.init.admin.password}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if(userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            log.info("✅ Администратор уже существует. Инициализация пропущена.");
            return;
        }

        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException("ADMIN_PASSWORD must contain at least 12 characters");
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(passwordUtil.encode(password));
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);

        userRepository.save(admin);
        log.warn("⚠️⚠️⚠️ СОЗДАН ПЕРВЫЙ АДМИНИСТРАТОР! ⚠️⚠️⚠️");

        log.warn("Пароль: изменен после первого входа");
        log.warn("❗ НЕМЕДЛЕННО СМЕНИТЕ ПАРОЛЬ ПОСЛЕ ПЕРВОГО ВХОДА!");
    }
}
