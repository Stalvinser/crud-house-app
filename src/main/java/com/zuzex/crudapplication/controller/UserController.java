package com.zuzex.crudapplication.controller;

import com.zuzex.crudapplication.dto.UserRequestDto;
import com.zuzex.crudapplication.dto.UserResponseDto;
import com.zuzex.crudapplication.model.User;
import com.zuzex.crudapplication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    @Operation(summary = "Получение списка всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class))})
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }
    @Operation(summary = "Получение пользователя по идентификатору")
    @ApiResponse(responseCode = "200", description = "Пользователь найден",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class))})
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable @NotNull Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @Operation(summary = "Создание нового пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class))})
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto user) {
        UserResponseDto createdUser = userService.saveUser(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdUser.id()).toUri();
        return ResponseEntity.created(location).body(createdUser);
    }
    @Operation(summary = "Обновление информации о пользователе")
    @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно обновлена",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class))})
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @RequestBody @Valid UserRequestDto user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
    @Operation(summary = "Удаление пользователя")
    @ApiResponse(responseCode = "204", description = "Пользователь успешно удален")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @NotNull Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
