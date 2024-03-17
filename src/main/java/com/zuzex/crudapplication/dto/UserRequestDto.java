package com.zuzex.crudapplication.dto;

import jakarta.validation.constraints.*;

public record UserRequestDto(@NotBlank(message = "Имя не должно быть пустым")
                             @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
                             String name,
                             @NotNull(message = "Возраст не может быть пустым")
                             @Min(value = 1, message = "Возраст должен быть не менее 1 года")
                             @Max(value = 100, message = "Возраст должен быть не более 100 лет")
                             Integer age,
                             @NotBlank(message = "Пароль не должен быть пустым")
                             @Size(min = 4, message = "Пароль должен содержать не менее 4 символов")
                             String password
) {

}

