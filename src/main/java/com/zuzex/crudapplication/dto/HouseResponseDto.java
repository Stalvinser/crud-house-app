package com.zuzex.crudapplication.dto;

import java.util.List;

public record HouseResponseDto(Long id,
                               String address,
                               Long ownerId,
                               List<Long> residentIds) {
}
