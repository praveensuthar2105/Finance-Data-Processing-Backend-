package com.finance.backend.service;

import com.finance.backend.dto.request.UpdateUserRequest;
import com.finance.backend.dto.response.UserResponse;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAllUsers(Pageable pageable, Role role) {
        Page<User> users;
        if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return mapToResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserById(id);
        user.setName(request.getName());
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUserRole(Long id, Role role) {
        User user = findUserById(id);
        user.setRole(role);
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUserStatus(Long id, Boolean isActive) {
        User user = findUserById(id);
        user.setIsActive(isActive);
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse mapToResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
}
