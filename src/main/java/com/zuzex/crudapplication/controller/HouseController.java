package com.zuzex.crudapplication.controller;

import com.zuzex.crudapplication.dto.HouseRequestDto;
import com.zuzex.crudapplication.dto.HouseResponseDto;
import com.zuzex.crudapplication.service.HouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/houses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class HouseController {

    private final HouseService houseService;

    @Operation(summary = "Получение списка всех домов")
    @ApiResponse(responseCode = "200", description = "Список домов успешно получен",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = HouseResponseDto.class))})
    @GetMapping
    public ResponseEntity<List<HouseResponseDto>> getAllHouses() {
        return ResponseEntity.ok(houseService.findAllHouses());
    }

    @Operation(summary = "Получение дома по идентификатору")
    @ApiResponse(responseCode = "200", description = "Дом найден",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = HouseResponseDto.class))})
    @GetMapping("/{id}")
    public ResponseEntity<HouseResponseDto> getHouseById(@PathVariable Long id) {
        return ResponseEntity.ok(houseService.findHouseById(id));
    }


    @Operation(summary = "Создание нового дома")
    @ApiResponse(responseCode = "201", description = "Дом успешно создан",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = HouseResponseDto.class))})
    @PostMapping
    public ResponseEntity<HouseResponseDto> createHouse(@RequestBody @Valid HouseRequestDto house) {
        HouseResponseDto createdHouse = houseService.createHouse(house);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdHouse.id()).toUri();
        return ResponseEntity.created(location).body(createdHouse);
    }


    @Operation(summary = "Обновление информации о доме")
    @ApiResponse(responseCode = "200", description = "Информация о доме успешно обновлена",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = HouseResponseDto.class))})
    @PutMapping("/{id}")
    public ResponseEntity<HouseResponseDto> updateHouse(@PathVariable Long id,
                                                        @RequestBody HouseRequestDto houseRequestDto,
                                                        Authentication authentication) {
        return ResponseEntity.ok(houseService.updateHouse(id, houseRequestDto, authentication.getName()));
    }

    @Operation(summary = "Удаление дома")
    @ApiResponse(responseCode = "204", description = "Дом успешно удален")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHouse(@PathVariable Long id,
                                         Authentication authentication) {
        houseService.deleteHouse(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавление жильца в дом")
    @ApiResponse(responseCode = "200", description = "Жилец успешно добавлен в дом",
            content = {@Content(mediaType = "application/json")})
    @PutMapping("/{houseId}/add-resident/{residentId}")
    public ResponseEntity<?> addResidentToHouse(@PathVariable Long houseId,
                                                @PathVariable Long residentId,
                                                Authentication authentication) {
        houseService.addResidentToHouse(houseId, residentId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удаление жильца из дома")
    @ApiResponse(responseCode = "200", description = "Жилец успешно удален из дома",
            content = {@Content(mediaType = "application/json")})
    @PutMapping("/{houseId}/remove-resident/{residentId}")
    public ResponseEntity<?> removeResidentFromHouse(@PathVariable Long houseId,
                                                     @PathVariable Long residentId,
                                                     Authentication authentication) {
        houseService.removeResidentFromHouse(houseId, residentId, authentication.getName());
        return ResponseEntity.ok().build();
    }

}
