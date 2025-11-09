package org.example.service;

import jakarta.validation.*;
import org.example.domain.User;
import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserResponse;
import org.example.exception.types.ConflictException;
import org.example.exception.types.NotFoundException;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse createUser(@Valid CreateUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        mailUnique(normalizedEmail);
        User user = userMapper.fromCreate(request);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse readUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, @Valid UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase();
            mailUnique(normalizedEmail);
            user.setEmail(normalizedEmail);
        }
        userMapper.applyUpdate(request, user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void removeUserById(Long id) {
        userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.deleteById(id);
    }

    @Transactional
    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponseList = new ArrayList<>();
        for (User user : users) {
            userResponseList.add(userMapper.toResponse(user));
        }
        return userResponseList;
    }

    public void mailUnique(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsUserByEmail(normalizedEmail)) {
            throw new ConflictException("Email already in use");
        }
    }
}
