package com.zuzex.crudapplication.service;

import com.zuzex.crudapplication.dto.UserRequestDto;
import com.zuzex.crudapplication.dto.UserResponseDto;
import com.zuzex.crudapplication.exception.UserException;
import com.zuzex.crudapplication.model.House;
import com.zuzex.crudapplication.model.User;
import com.zuzex.crudapplication.repository.UserRepository;
import com.zuzex.crudapplication.security.auth.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        List<User> userList = userRepository.findAll();
        if (userList.isEmpty()) {
            return Collections.emptyList();
        }
        return userList.stream()
                .map(UserService::mapUserEntityToResponseDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserService::mapUserEntityToResponseDto)
                .orElseThrow(() -> new UserException("Не найден пользователь с id: " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public UserResponseDto saveUser(UserRequestDto registrationUser) {
        if (userRepository.existsByName(registrationUser.name())) {
            throw new AuthenticationException("Посльзователь с именем " + registrationUser.name() + " уже существует",
                    HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .name(registrationUser.name())
                .age(registrationUser.age())
                .password(passwordEncoder.encode(registrationUser.password()))
                .build();
        User savedUser = userRepository.save(user);
        return mapUserEntityToResponseDto(savedUser);
    }
    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserException("Не найден пользователь с id: " + id, HttpStatus.NOT_FOUND));
        userRepository.deleteById(id);
    }
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Не найден пользователь с id: " + id, HttpStatus.NOT_FOUND));
        if (userRequestDto.name() != null) user.setName(userRequestDto.name());
        if (userRequestDto.age() != null) user.setAge(userRequestDto.age());
        if (userRequestDto.password() != null) user.setPassword(passwordEncoder.encode(userRequestDto.password()));
        User updatedUser = userRepository.save(user);
        return mapUserEntityToResponseDto(updatedUser);
    }

    private static UserResponseDto mapUserEntityToResponseDto(User user) {
        List<Long> ownedHouseIds = Optional.ofNullable(user.getOwnedHouses())
                .orElse(Collections.emptyList())
                .stream()
                .map(House::getId)
                .collect(Collectors.toList());

        List<Long> residenceIds = Optional.ofNullable(user.getResidences())
                .orElse(Collections.emptyList())
                .stream()
                .map(House::getId)
                .collect(Collectors.toList());

        return new UserResponseDto(user.getId(), user.getName(), user.getAge(), ownedHouseIds, residenceIds);
    }



}
