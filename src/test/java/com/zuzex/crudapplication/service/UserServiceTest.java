package com.zuzex.crudapplication.service;

import com.zuzex.crudapplication.dto.UserRequestDto;
import com.zuzex.crudapplication.dto.UserResponseDto;
import com.zuzex.crudapplication.exception.UserException;
import com.zuzex.crudapplication.model.User;
import com.zuzex.crudapplication.repository.UserRepository;
import com.zuzex.crudapplication.security.auth.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").age(30).password("password").build();
        userRequestDto = new UserRequestDto("Test User", 30, "password");
    }

    @Test
    void findAllUsers_whenNoUsersExist_returnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        assertTrue(userService.findAllUsers().isEmpty());
        verify(userRepository).findAll();
    }
    @Test
    void findAllUsers_whenUsersExist_returnUserList() {
        List<User> users = Collections.singletonList(user);
        when(userRepository.findAll()).thenReturn(users);
        List<UserResponseDto> result = userService.findAllUsers();
        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).id());
    }

    @Test
    void getUserById_WhenUserExists() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        UserResponseDto responseDto = userService.getUserById(1L);
        assertNotNull(responseDto);
        assertEquals(user.getName(), responseDto.name());
        verify(userRepository).findById(any());
    }

    @Test
    void getUserById_WhenUserDoesNotExist() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(UserException.class, () -> userService.getUserById(1L));
        verify(userRepository).findById(any());
    }

    @Test
    void saveUser_WhenUserDoesNotExist() {
        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserResponseDto savedUser = userService.saveUser(userRequestDto);
        assertNotNull(savedUser);
        assertEquals(user.getName(), savedUser.name());

        verify(userRepository).existsByName(anyString());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
    }
    @Test
    void saveUser_WhenUserExists() {
        when(userRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> userService.saveUser(userRequestDto));

        verify(userRepository).existsByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUser(1L);

        verify(userRepository).findById(anyLong());
        verify(userRepository).deleteById(anyLong());
    }

    @Test
    void deleteUser_WhenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UserException.class, () -> userService.deleteUser(1L));
        verify(userRepository).findById(anyLong());
    }

    @Test
    void updateUser_WhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserResponseDto updatedUser = userService.updateUser(1L, userRequestDto);

        assertNotNull(updatedUser);
        assertEquals(user.getId(), updatedUser.id());
        assertEquals(user.getName(), updatedUser.name());

        verify(userRepository).findById(anyLong());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void updateUser_WhenUserDoesNotExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> userService.updateUser(1L, userRequestDto));

        verify(userRepository).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }




}