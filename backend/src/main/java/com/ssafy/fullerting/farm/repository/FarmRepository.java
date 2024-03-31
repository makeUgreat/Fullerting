package com.ssafy.fullerting.farm.repository;

import com.ssafy.fullerting.farm.model.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByAreaLcd(Integer areaLcd);
}
