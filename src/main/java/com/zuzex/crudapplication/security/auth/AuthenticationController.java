package com.zuzex.crudapplication.security.auth;

import com.zuzex.crudapplication.dto.UserRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService service;
    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя и возвращает токен аутентификации.")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid UserRequestDto userDto) {
        return ResponseEntity.ok(service.register(userDto));
    }
    @Operation(summary = "Аутентификация пользователя",
            description = "Проверяет учетные данные пользователя и возвращает токен аутентификации.")
    @ApiResponse(responseCode = "200", description = "Аутентификация прошла успешно",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
