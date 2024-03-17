package com.zuzex.crudapplication.service;

import com.zuzex.crudapplication.dto.HouseRequestDto;
import com.zuzex.crudapplication.dto.HouseResponseDto;
import com.zuzex.crudapplication.exception.HouseException;
import com.zuzex.crudapplication.exception.OwnerException;
import com.zuzex.crudapplication.exception.ResidentException;
import com.zuzex.crudapplication.exception.UserException;
import com.zuzex.crudapplication.model.House;
import com.zuzex.crudapplication.model.User;
import com.zuzex.crudapplication.repository.HouseRepository;
import com.zuzex.crudapplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseService {

    private final HouseRepository houseRepository;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public List<HouseResponseDto> findAllHouses() {
        List<House> houseList =  houseRepository.findAll();
        if (houseList.isEmpty()) {
            return Collections.emptyList();
        }
        return houseList.stream()
                .map(HouseService::mapHouseEntityToResponseDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public HouseResponseDto findHouseById(Long id) {
        return houseRepository.findById(id)
                .map(HouseService::mapHouseEntityToResponseDto)
                .orElseThrow(() -> new HouseException("Не найден дом с id: " + id, HttpStatus.NOT_FOUND));
    }
    @Transactional
    public HouseResponseDto createHouse(HouseRequestDto houseRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User owner = userRepository.findByName(name)
                .orElseThrow(() -> new UserException("Пользователь не найден: " + name, HttpStatus.NOT_FOUND));
        if (houseRepository.existsByAddress(houseRequestDto.address())) {
            throw new HouseException("Дом с таким адресом " + houseRequestDto.address() + " уже зарегестрирован",
                    HttpStatus.BAD_REQUEST);
        }
        House house = House.builder()
                .address(houseRequestDto.address())
                .owner(owner)
                .build();
        House savedHouse = houseRepository.save(house);
        return mapHouseEntityToResponseDto(savedHouse);
    }

    @Transactional
    public HouseResponseDto updateHouse(Long id, HouseRequestDto houseRequestDto, String ownerUsername) {
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new HouseException("Не найден дом с id: " + id, HttpStatus.NOT_FOUND));
        User requester = userRepository.findByName(ownerUsername)
                .orElseThrow(() -> new UserException("Пользователь не найден: " + ownerUsername, HttpStatus.NOT_FOUND));
        if (!house.getOwner().equals(requester)) {
            throw new OwnerException("Только владец дома может его редактировать", HttpStatus.FORBIDDEN);
        }
        house.setAddress(houseRequestDto.address());
        House updatedHouse = houseRepository.save(house);
        return mapHouseEntityToResponseDto(updatedHouse);
    }
    @Transactional
    public void deleteHouse(Long id, String ownerUsername) {
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new HouseException("Не найден дом с id: " + id, HttpStatus.NOT_FOUND));
        User requester = userRepository.findByName(ownerUsername)
                .orElseThrow(() -> new UserException("Владелец дома не найден: " + ownerUsername, HttpStatus.NOT_FOUND));
        if (!house.getOwner().equals(requester)) {
            throw new OwnerException("Только владец дома может его удалить", HttpStatus.FORBIDDEN);
        }
        if (house.getResidents() != null && !house.getResidents().isEmpty()) {
            house.getResidents().forEach(resident -> resident.getResidences().remove(house));
            userRepository.saveAll(house.getResidents());
        }
        houseRepository.delete(house);
    }

    @Transactional
    public void addResidentToHouse(Long houseId, Long residentId, String ownerUsername) {
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new HouseException("Не найден дом с id: " + houseId, HttpStatus.NOT_FOUND));
        User resident = userRepository.findById(residentId)
                .orElseThrow(() -> new ResidentException("Жилец не найден: " + residentId, HttpStatus.NOT_FOUND));
        if (!house.getOwner().getUsername().equals(ownerUsername)) {
            throw new HouseException("Только владец дома может добавлсять жильцов", HttpStatus.FORBIDDEN);
        }
        if (house.getResidents() == null) {
            house.setResidents(new ArrayList<>());
        }
        if (!house.getResidents().contains(resident)) {
            house.getResidents().add(resident);
            houseRepository.save(house);
        } else {
            throw new ResidentException("Жилец уже зарегистрирован в этом доме", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void removeResidentFromHouse(Long houseId, Long residentId, String ownerUsername) {
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new HouseException("Не найден дом с id: " + houseId, HttpStatus.NOT_FOUND));
        User resident = userRepository.findById(residentId)
                .orElseThrow(() -> new ResidentException("Жилец не найден: " + residentId, HttpStatus.NOT_FOUND));
        if (!house.getOwner().getUsername().equals(ownerUsername)) {
            throw new HouseException("Только владец дома может удалять жильцов", HttpStatus.FORBIDDEN);
        }
        if (house.getResidents() == null) {
            throw new ResidentException("Жилец не зарегистрирован в этом доме", HttpStatus.BAD_REQUEST);
        }
        if (house.getResidents().contains(resident)) {
            house.getResidents().remove(resident);
            houseRepository.save(house);
        } else {
            throw new ResidentException("Жилец не зарегистрирован в этом доме", HttpStatus.BAD_REQUEST);
        }
    }

    private static HouseResponseDto mapHouseEntityToResponseDto(House house) {
        Long ownerId = house.getOwner() != null ? house.getOwner().getId() : null;

        List<Long> residentIds = Optional.ofNullable(house.getResidents())
                .orElse(Collections.emptyList())
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        return new HouseResponseDto(house.getId(), house.getAddress(), ownerId, residentIds);
    }
}
