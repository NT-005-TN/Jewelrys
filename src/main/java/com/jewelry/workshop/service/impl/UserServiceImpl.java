package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.user.UserCreateDTO;
import com.jewelry.workshop.domain.model.dto.user.UserDeleteResponseDTO;
import com.jewelry.workshop.domain.model.dto.user.UserResponseDTO;
import com.jewelry.workshop.domain.model.dto.user.UserUpdateDTO;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.model.entity.Employee;
import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.ClientRepository;
import com.jewelry.workshop.domain.repository.EmployeeRepository;
import com.jewelry.workshop.domain.repository.UserRepository;
import com.jewelry.workshop.service.interfaces.UserService;
import com.jewelry.workshop.service.mapper.UserMapper;
import com.jewelry.workshop.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        List<UserResponseDTO> dtos = users.getContent().stream()
                .map(user -> {
                    Long clientId = clientRepository.findByUserId(user.getId())
                            .map(Client::getId)
                            .orElse(null);
                    Long employeeId = employeeRepository.findByUserId(user.getId())
                            .map(Employee::getId)
                            .orElse(null);
                    return userMapper.toDto(user, clientId, employeeId);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, users.getTotalElements());
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        String username = dto.getUsername().trim();
        User.Role role = parseRole(dto.getRole());

        if(userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Имя пользователя " + username + " уже занято");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordUtil.encode(dto.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerified(true); // ← Админ создаёт сразу активного пользователя

        User savedUser = userRepository.save(user);

        ensureProfileForRole(savedUser, role, username);

        Long clientId = User.Role.CLIENT.equals(user.getRole()) ?
                clientRepository.findByUserId(savedUser.getId()).map(Client::getId).orElse(null) : null;

        Long employeeId = !User.Role.CLIENT.equals(user.getRole()) ?
                employeeRepository.findByUserId(savedUser.getId()).map(Employee::getId).orElse(null) : null;

        return userMapper.toDto(savedUser, clientId, employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        Long clientId = null;
        Long employeeId = null;

        if (User.Role.CLIENT.equals(user.getRole())) {
            clientId = clientRepository.findByUserId(user.getId())
                    .map(Client::getId)
                    .orElse(null);
        } else {
            employeeId = employeeRepository.findByUserId(user.getId())
                    .map(Employee::getId)
                    .orElse(null);
        }

        return userMapper.toDto(user, clientId, employeeId);
    }


    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto, Long currentUserId) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        String oldEmail = existingUser.getEmail();
        String newEmail = dto.getEmail() == null ? oldEmail : normalizeEmail(dto.getEmail());
        if (!existingUser.getEmail().equalsIgnoreCase(newEmail) &&
                userRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new RuntimeException("Email " + newEmail + " уже используется");
        }

        String newUsername = dto.getUsername() == null ? existingUser.getUsername() : dto.getUsername().trim();
        if (!existingUser.getUsername().equals(newUsername) &&
                userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Имя пользователя " + newUsername + " уже занято");
        }

        User.Role newRole = dto.getRole() == null ? existingUser.getRole() : parseRole(dto.getRole());
        boolean roleChanged = !existingUser.getRole().equals(newRole);
        boolean willDisable = Boolean.FALSE.equals(dto.getEnabled()) && existingUser.isEnabled();

        if (id.equals(currentUserId) && (willDisable || (existingUser.isAdmin() && !User.Role.ADMIN.equals(newRole)))) {
            throw new IllegalArgumentException("Нельзя деактивировать себя или снять с себя роль администратора");
        }
        guardLastActiveAdmin(existingUser, newRole, willDisable);

        existingUser.setUsername(newUsername);
        existingUser.setEmail(newEmail);
        existingUser.setRole(newRole);
        if (dto.getEnabled() != null) {
            existingUser.setEnabled(dto.getEnabled());
        }

        if (roleChanged) {
            ensureProfileForRole(existingUser, newRole, newUsername);
        }

        User savedUser = userRepository.save(existingUser);

        if (roleChanged || willDisable || !oldEmail.equalsIgnoreCase(newEmail)) {
            revokeRefreshTokens(oldEmail, newEmail);
        }

        Long clientId = null;
        Long employeeId = null;

        if (newRole == User.Role.CLIENT) {
            clientId = clientRepository.findByUserId(savedUser.getId())
                    .map(Client::getId).orElse(null);
        } else {
            employeeId = employeeRepository.findByUserId(savedUser.getId())
                    .map(Employee::getId).orElse(null);
        }

        return userMapper.toDto(savedUser, clientId, employeeId);
    }

    @Override
    @Transactional
    public UserDeleteResponseDTO deleteUser(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new RuntimeException("Нельзя деактивировать самого себя");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        guardLastActiveAdmin(user, user.getRole(), true);
        user.setEnabled(false);
        userRepository.save(user);
        revokeRefreshTokens(user.getEmail(), user.getEmail());

        UserDeleteResponseDTO response = new UserDeleteResponseDTO();
        response.setMessage("Пользователь успешно деактивирован");
        return response;
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(Long id, String newRoleStr, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        User.Role newRole = parseRole(newRoleStr);
        if (user.getRole().equals(newRole)) {
            // Роль не изменилась
            Long clientId = User.Role.CLIENT.equals(user.getRole()) ?
                    clientRepository.findByUserId(user.getId()).map(Client::getId).orElse(null) : null;
            Long employeeId = !User.Role.CLIENT.equals(user.getRole()) ?
                    employeeRepository.findByUserId(user.getId()).map(Employee::getId).orElse(null) : null;
            return userMapper.toDto(user, clientId, employeeId);
        }

        if (id.equals(currentUserId) && user.isAdmin() && !User.Role.ADMIN.equals(newRole)) {
            throw new IllegalArgumentException("Нельзя снять с себя роль администратора");
        }
        guardLastActiveAdmin(user, newRole, false);

        ensureProfileForRole(user, newRole, user.getUsername());

        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        revokeRefreshTokens(user.getEmail(), user.getEmail());

        Long clientId = null;
        Long employeeId = null;
        if (newRole == User.Role.CLIENT) {
            clientId = clientRepository.findByUserId(savedUser.getId())
                    .map(Client::getId).orElse(null);
        } else {
            employeeId = employeeRepository.findByUserId(savedUser.getId())
                    .map(Employee::getId).orElse(null);
        }

        return userMapper.toDto(savedUser, clientId, employeeId);
    }

    private void ensureProfileForRole(User user, User.Role role, String usernameFallback) {
        if (User.Role.CLIENT.equals(role)) {
            if (clientRepository.findByUserId(user.getId()).isEmpty()) {
                Client client = new Client();
                client.setUser(user);
                client.setFirstName(usernameFallback);
                client.setLastName("Клиент");
                clientRepository.save(client);
            }
            return;
        }

        if (employeeRepository.findByUserId(user.getId()).isEmpty()) {
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setPosition("Сотрудник");
            employee.setDepartment("Администрация");
            employeeRepository.save(employee);
        }
    }

    private void guardLastActiveAdmin(User user, User.Role newRole, boolean willDisable) {
        if (user.isAdmin() && user.isEnabled()
                && (willDisable || !User.Role.ADMIN.equals(newRole))
                && userRepository.countByRoleAndEnabled(User.Role.ADMIN, true) <= 1) {
            throw new IllegalArgumentException("Нельзя деактивировать или понизить последнего активного администратора");
        }
    }

    private User.Role parseRole(String role) {
        try {
            return User.Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Недопустимая роль: " + role);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void revokeRefreshTokens(String oldEmail, String newEmail) {
        redisTemplate.delete("refresh_token:" + oldEmail);
        if (!oldEmail.equalsIgnoreCase(newEmail)) {
            redisTemplate.delete("refresh_token:" + newEmail);
        }
    }
}
