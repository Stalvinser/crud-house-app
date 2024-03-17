package com.zuzex.crudapplication.controller;

import com.zuzex.crudapplication.dto.HouseRequestDto;
import com.zuzex.crudapplication.dto.HouseResponseDto;
import com.zuzex.crudapplication.dto.UserResponseDto;
import com.zuzex.crudapplication.model.House;
import com.zuzex.crudapplication.service.HouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/houses")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    @GetMapping
    public ResponseEntity<List<HouseResponseDto>> getAllHouses() {
        return ResponseEntity.ok(houseService.findAllHouses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HouseResponseDto> getHouseById(@PathVariable Long id) {
        return ResponseEntity.ok(houseService.findHouseById(id));
    }

    @PostMapping
    public ResponseEntity<HouseResponseDto> createHouse(@RequestBody @Valid HouseRequestDto house) {
        HouseResponseDto createdHouse = houseService.createHouse(house);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdHouse.id()).toUri();
        return ResponseEntity.created(location).body(createdHouse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HouseResponseDto> updateHouse(@PathVariable Long id,
                                                        @RequestBody HouseRequestDto houseRequestDto,
                                                        Authentication authentication) {
        return ResponseEntity.ok(houseService.updateHouse(id, houseRequestDto, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHouse(@PathVariable Long id,
                                         Authentication authentication) {
        houseService.deleteHouse(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{houseId}/add-resident/{residentId}")
    public ResponseEntity<?> addResidentToHouse(@PathVariable Long houseId,
                                                @PathVariable Long residentId,
                                                Authentication authentication) {
        houseService.addResidentToHouse(houseId, residentId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{houseId}/remove-resident/{residentId}")
    public ResponseEntity<?> removeResidentFromHouse(@PathVariable Long houseId,
                                                     @PathVariable Long residentId,
                                                     Authentication authentication) {
        houseService.removeResidentFromHouse(houseId, residentId, authentication.getName());
        return ResponseEntity.ok().build();
    }

}
