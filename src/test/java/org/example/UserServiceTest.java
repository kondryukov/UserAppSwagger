package org.example;

import org.example.domain.User;
import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserResponse;
import org.example.exception.types.ConflictException;
import org.example.exception.types.NotFoundException;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper mapper;
    @InjectMocks
    UserService service;

    @Test
    void createThrowsConflictWhenEmailExists() {
        when(userRepository.existsUserByEmail("name@mail.ru")).thenReturn(true);

        RuntimeException exception = assertThrows(ConflictException.class,
                () -> service.createUser(new CreateUserRequest("name", "name@mail.ru", 1)));
        assertThat(exception.getMessage()).isEqualTo("Email already in use");
    }

    @Test
    void updateThrowsNotFoundWhenUserMissing() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(NotFoundException.class,
                () -> service.updateUser(42L, new UpdateUserRequest("name", "name@mail.ru", 1)));
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    @Test
    void createUser() {
        User user = new User();
        user.setName("name");
        user.setAge(1);
        user.setEmail("name@mail.ru");
        user.setId(1L);
        Date date = new Date();
        user.setCreatedAt(date);
        user.setUpdatedAt(date);

        CreateUserRequest createUserRequest = new CreateUserRequest("name", "naME@mail.ru", 1);
        UserResponse userResponse = new UserResponse(1L, "name", "name@mail.ru", 1, date, date);

        when(userRepository.existsUserByEmail("name@mail.ru")).thenReturn(false);
        when(mapper.fromCreate(createUserRequest)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(userResponse);
        when(userRepository.save(user)).thenReturn(user);


        assertThat(service.createUser(createUserRequest)).isNotNull();
        assertThat(service.createUser(createUserRequest)).isEqualTo(userResponse);
    }

    @Test
    void readUser() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UserResponse userResponse = new UserResponse(1L, "name", "name@mail.ru", 1, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(userResponse);

        assertThat(service.readUser(1L)).isEqualTo(userResponse);
    }

    @Test
    void readNotExistingUser() {
        when(userRepository.findById(1L)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> service.readUser(1L));
    }

    @Test
    void updateUser() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, "newName@mail.ru", 123);
        UserResponse userResponse = new UserResponse(1L, "name", "newname@mail.ru", 1, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail("newname@mail.ru")).thenReturn(false);

        when(mapper.toResponse(user)).thenReturn(userResponse);

        assertThat(service.updateUser(1L, updateUserRequest)).isNotNull();
        assertThat(service.updateUser(1L, updateUserRequest)).isEqualTo(userResponse);
    }

    @Test
    void updateUserDuplicateEmail() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, "newName@mail.ru", 123);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail("newname@mail.ru")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.updateUser(1L, updateUserRequest));
        assertThat(exception.getMessage()).isEqualTo("Email already in use");
    }

    @Test
    void removeUserByNotExistingId() {
        when(userRepository.findById(1L)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class,
                () -> service.removeUserById(1L));
    }

    @Test
    void mailValidAndUnique() {
        String email = "name@mail.ru";
        when(userRepository.existsUserByEmail(email)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.mailUnique(email));
        assertThat(exception.getMessage()).isEqualTo("Email already in use");
    }
}
