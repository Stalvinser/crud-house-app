package com.zuzex.crudapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HouseRequestDto(@NotBlank(message = "Адрес не должен быть пустым")
                              @Size(min = 2, max = 50, message = "Адрес должен содержать от 2 до 50 символов")
                              String address) {
}
