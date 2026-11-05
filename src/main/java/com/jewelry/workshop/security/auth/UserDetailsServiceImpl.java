package com.jewelry.workshop.security.auth;

import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetailsImpl loadUserById(Long id) {
        return UserDetailsImpl.build(userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден")));
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));
        return UserDetailsImpl.build(user);
    }
}