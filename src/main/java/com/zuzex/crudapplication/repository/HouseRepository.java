package com.zuzex.crudapplication.repository;

import com.zuzex.crudapplication.model.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {
    boolean existsByAddress(String name);
}
