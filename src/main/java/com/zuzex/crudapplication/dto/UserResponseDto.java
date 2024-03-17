package com.zuzex.crudapplication.dto;

import java.util.List;

public record UserResponseDto(Long id,
                              String name,
                              Integer age,
                              List<Long> ownedHouseIds,
                              List<Long> residenceIds) {
}
