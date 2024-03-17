package com.zuzex.crudapplication.service;


import com.zuzex.crudapplication.dto.HouseRequestDto;
import com.zuzex.crudapplication.dto.HouseResponseDto;
import com.zuzex.crudapplication.exception.HouseException;
import com.zuzex.crudapplication.exception.OwnerException;
import com.zuzex.crudapplication.exception.ResidentException;
import com.zuzex.crudapplication.model.House;
import com.zuzex.crudapplication.model.User;
import com.zuzex.crudapplication.repository.HouseRepository;
import com.zuzex.crudapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseServiceTest {
    @Mock
    private HouseRepository houseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HouseService houseService;

    private House house;
    private User owner;
    private User resident;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "OwnerUser", 30, "password", new ArrayList<>(), new ArrayList<>());
        resident = new User(2L, "ResidentUsername", 25, "residentPassword", new ArrayList<>(), new ArrayList<>());
        house = House.builder().id(1L).address("123 Main St").owner(owner).residents(new ArrayList<>()).build();
    }

    @Test
    void findAllHouses_ReturnsAllHouses() {
        when(houseRepository.findAll()).thenReturn(List.of(house));
        List<HouseResponseDto> result = houseService.findAllHouses();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(house.getId(), result.get(0).id());
    }

    @Test
    void findHouseById_WhenHouseExists() {
        when(houseRepository.findById(1L)).thenReturn(Optional.of(house));
        HouseResponseDto result = houseService.findHouseById(1L);
        assertEquals(house.getId(), result.id());
        assertEquals(house.getAddress(), result.address());
    }

    @Test
    void findHouseById_WhenHouseDoesNotExist_ThrowsException() {
        when(houseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(HouseException.class, () -> houseService.findHouseById(1L));
    }

    @Test
    void createHouse_WhenHouseDoesNotExist_CreatesHouse() {
        HouseRequestDto houseRequestDto = new HouseRequestDto("123 Main St");
        when(userRepository.findByName("OwnerUser")).thenReturn(Optional.of(owner));
        when(houseRepository.existsByAddress("123 Main St")).thenReturn(false);
        when(houseRepository.save(any(House.class))).thenReturn(house);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("OwnerUser");

        HouseResponseDto result = houseService.createHouse(houseRequestDto);
        assertNotNull(result);
        assertEquals(house.getAddress(), result.address());

        SecurityContextHolder.clearContext();
    }

    @Test
    void createHouse_WhenHouseExists_ThrowsException() {
        HouseRequestDto houseRequestDto = new HouseRequestDto("123 Main St");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("OwnerUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName("OwnerUser")).thenReturn(Optional.of(owner));
        when(houseRepository.existsByAddress(houseRequestDto.address())).thenReturn(true);
        assertThrows(HouseException.class, () -> houseService.createHouse(houseRequestDto));

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateHouse_WhenOwnerUpdatesHouse() {
        HouseRequestDto houseRequestDto = new HouseRequestDto("New Address");
        mockAuthentication(owner.getUsername());

        when(userRepository.findByName(owner.getUsername())).thenReturn(Optional.of(owner));
        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(houseRepository.save(any(House.class))).thenReturn(house);

        HouseResponseDto updatedHouse = houseService.updateHouse(house.getId(), houseRequestDto, owner.getUsername());

        assertEquals("New Address", updatedHouse.address());
        verify(houseRepository).save(house);
    }


    @Test
    void updateHouse_WhenNonOwnerTriesToUpdateHouse_ThrowsException() {
        HouseRequestDto houseRequestDto = new HouseRequestDto("New Address");
        String nonOwnerUsername = "nonOwnerUser";
        mockAuthentication(nonOwnerUsername);

        when(userRepository.findByName(nonOwnerUsername)).thenReturn(Optional.of(new User()));
        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));

        assertThrows(OwnerException.class, () -> houseService.updateHouse(house.getId(), houseRequestDto, nonOwnerUsername));
    }


    @Test
    void deleteHouse_WhenOwnerDeletesHouseWithResidents() {
        List<User> residents = List.of(resident);
        house.setResidents(residents);
        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findByName(owner.getUsername())).thenReturn(Optional.of(owner));

        houseService.deleteHouse(house.getId(), owner.getUsername());

        verify(houseRepository).delete(house);
        verify(userRepository).saveAll(residents);
    }


    @Test
    void deleteHouse_WhenOwnerDeletesHouseWithoutResidents() {
        house.setResidents(new ArrayList<>());
        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findByName(owner.getUsername())).thenReturn(Optional.of(owner));

        houseService.deleteHouse(house.getId(), owner.getUsername());

        verify(houseRepository).delete(house);
        verify(userRepository, times(0)).saveAll(anyList());
    }


    @Test
    void deleteHouse_WhenNonOwnerTriesToDeleteHouse_ThrowsException() {
        String nonOwnerUsername = "nonOwnerUser";
        mockAuthentication(nonOwnerUsername);

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findByName(nonOwnerUsername)).thenReturn(Optional.of(new User()));

        assertThrows(OwnerException.class, () -> houseService.deleteHouse(house.getId(), nonOwnerUsername));
    }

    @Test
    void addResidentToHouse_Successful() {
        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        assertDoesNotThrow(() -> houseService.addResidentToHouse(house.getId(), resident.getId(), owner.getUsername()));

        assertTrue(house.getResidents().contains(resident));
        verify(houseRepository).save(house);
    }

    @Test
    void addResidentToHouse_ResidentAlreadyExists_ThrowsException() {
        mockAuthentication(owner.getUsername());
        house.getResidents().add(resident);

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        ResidentException exception = assertThrows(ResidentException.class, () -> houseService.addResidentToHouse(house.getId(), resident.getId(), owner.getUsername()));
        assertEquals("Жилец уже зарегистрирован в этом доме", exception.getMessage());
    }


    @Test
    void removeResidentFromHouse_Successful() {
        house.getResidents().add(resident);

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        assertDoesNotThrow(() -> houseService.removeResidentFromHouse(
                house.getId(), resident.getId(), owner.getUsername()));

        assertFalse(house.getResidents().contains(resident));
        verify(houseRepository).save(house);
    }

    @Test
    void removeResidentFromHouse_ResidentNotPresent_ThrowsException() {
        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        assertThrows(ResidentException.class,
                () -> houseService.removeResidentFromHouse(house.getId(), resident.getId(), owner.getUsername()));
    }


    @Test
    void addResidentToHouse_Successful_IfOwner() {
        mockAuthentication(owner.getUsername());

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        houseService.addResidentToHouse(house.getId(), resident.getId(), owner.getUsername());
        assertTrue(house.getResidents().contains(resident));
        verify(houseRepository).save(house);
    }


    @Test
    void addResidentToHouse_Fails_IfNotOwner() {
        String notOwnerUsername = "notOwnerUsername";
        mockAuthentication(notOwnerUsername);
        lenient().when(userRepository.findByName(notOwnerUsername)).thenReturn(Optional.of(new User()));
        assertThrows(HouseException.class,
                () -> houseService.addResidentToHouse(house.getId(), resident.getId(), notOwnerUsername));

    }


    @Test
    void removeResidentFromHouse_Successful_IfOwner() {
        mockAuthentication(owner.getUsername());
        house.getResidents().add(resident);

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        assertDoesNotThrow(() -> houseService.removeResidentFromHouse(house.getId(), resident.getId(), owner.getUsername()));
        assertFalse(house.getResidents().contains(resident));
    }

    @Test
    void removeResidentFromHouse_Fails_IfNotOwner() {
        String notOwnerUsername = "notOwnerUsername";
        mockAuthentication(notOwnerUsername);
        house.getResidents().add(resident);


        lenient().when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        lenient().when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        lenient().when(userRepository.findByName(notOwnerUsername)).thenReturn(Optional.of(new User()));

        assertThrows(HouseException.class,
                () -> houseService.removeResidentFromHouse(house.getId(), resident.getId(), notOwnerUsername));
    }


    private void mockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

}