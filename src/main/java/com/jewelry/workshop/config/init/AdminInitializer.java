package com.jewelry.workshop.config.init;

import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.UserRepository;
import com.jewelry.workshop.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if(userRepository.existsByUsername("admin") || userRepository.existsByEmail("admin@jewelry.local")) {
            log.info("✅ Администратор уже существует. Инициализация пропущена.");
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@jewelry.local");
        admin.setPasswordHash(passwordUtil.encode("SecureAdminPass123!"));
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);

        userRepository.save(admin);
        log.warn("⚠️⚠️⚠️ СОЗДАН ПЕРВЫЙ АДМИНИСТРАТОР! ⚠️⚠️⚠️");
        log.warn("Логин: admin@jewelry.local");
        log.warn("Пароль: изменен после первого входа");
        log.warn("❗ НЕМЕДЛЕННО СМЕНИТЕ ПАРОЛЬ ПОСЛЕ ПЕРВОГО ВХОДА!");
    }
}
