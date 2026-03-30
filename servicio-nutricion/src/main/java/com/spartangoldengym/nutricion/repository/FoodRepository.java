package com.spartangoldengym.nutricion.repository;

import com.spartangoldengym.nutricion.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    Optional<Food> findByBarcode(String barcode);

    List<Food> findByRegion(String region);
}
