package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.user.UserCreateDTO;
import com.jewelry.workshop.domain.model.dto.user.UserDeleteResponseDTO;
import com.jewelry.workshop.domain.model.dto.user.UserResponseDTO;
import com.jewelry.workshop.domain.model.dto.user.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDTO createUser(UserCreateDTO dto);

    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUser(Long id, UserUpdateDTO dto);
    UserResponseDTO updateUserRole(Long id, String newRole);

    UserDeleteResponseDTO deleteUser(Long id, Long currentUserId);
}

