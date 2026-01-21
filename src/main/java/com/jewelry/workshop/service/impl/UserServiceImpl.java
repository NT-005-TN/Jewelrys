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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;

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
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Пользователь с email " + dto.getEmail() + " уже существует");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Имя пользователя " + dto.getUsername() + " уже занято");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordUtil.encode(dto.getPassword()));
        user.setRole(User.Role.valueOf(dto.getRole()));
        user.setEnabled(true);
        user.setEmailVerified(true); // ← Админ создаёт сразу активного пользователя

        User savedUser = userRepository.save(user);

        if (User.Role.CLIENT.equals(user.getRole())) {
            Client client = new Client();
            client.setUser(savedUser);
            client.setFirstName(dto.getUsername());
            client.setLastName("Клиент");
            clientRepository.save(client);
        } else if (User.Role.SELLER.equals(user.getRole()) || User.Role.ADMIN.equals(user.getRole())) {
            Employee employee = new Employee();
            employee.setUser(savedUser);
            employee.setPosition("Сотрудник");
            employee.setDepartment("Администрация");
            employeeRepository.save(employee);
        }

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
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        if (!existingUser.getEmail().equals(dto.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email " + dto.getEmail() + " уже используется");
        }

        if (!existingUser.getUsername().equals(dto.getUsername()) &&
                userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Имя пользователя " + dto.getUsername() + " уже занято");
        }

        User.Role newRole = User.Role.valueOf(dto.getRole());
        boolean roleChanged = !existingUser.getRole().equals(newRole);

        if (roleChanged) {
            if (existingUser.getRole() == User.Role.CLIENT) {
                clientRepository.deleteByUserId(existingUser.getId());
            } else {
                employeeRepository.deleteByUserId(existingUser.getId());
            }

            if (newRole == User.Role.CLIENT) {
                Client client = new Client();
                client.setUser(existingUser);
                client.setFirstName(dto.getUsername());
                client.setLastName("Клиент");
                clientRepository.save(client);
            } else {
                Employee employee = new Employee();
                employee.setUser(existingUser);
                employee.setPosition("Сотрудник");
                employee.setDepartment("Администрация");
                employeeRepository.save(employee);
            }
        }

        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());
        existingUser.setRole(newRole);
        existingUser.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        User savedUser = userRepository.save(existingUser);

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

        user.setEnabled(false);
        userRepository.save(user);

        UserDeleteResponseDTO response = new UserDeleteResponseDTO();
        response.setMessage("Пользователь успешно деактивирован");
        return response;
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(Long id, String newRoleStr) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        User.Role newRole = User.Role.valueOf(newRoleStr);
        if (user.getRole().equals(newRole)) {
            // Роль не изменилась
            Long clientId = User.Role.CLIENT.equals(user.getRole()) ?
                    clientRepository.findByUserId(user.getId()).map(Client::getId).orElse(null) : null;
            Long employeeId = !User.Role.CLIENT.equals(user.getRole()) ?
                    employeeRepository.findByUserId(user.getId()).map(Employee::getId).orElse(null) : null;
            return userMapper.toDto(user, clientId, employeeId);
        }

        // Сначала удаляем старый профиль
        if (user.getRole() == User.Role.CLIENT) {
            clientRepository.deleteByUserId(user.getId());
        } else {
            employeeRepository.deleteByUserId(user.getId());
        }

        // Создаем новый профиль
        if (newRole == User.Role.CLIENT) {
            Client client = new Client();
            client.setUser(user);
            client.setFirstName(user.getUsername());
            client.setLastName("Клиент");
            clientRepository.save(client);
        } else {
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setPosition("Сотрудник");
            employee.setDepartment("Администрация");
            employeeRepository.save(employee);
        }

        user.setRole(newRole);
        User savedUser = userRepository.save(user);

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
}