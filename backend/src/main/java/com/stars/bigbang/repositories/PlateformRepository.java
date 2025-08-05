package com.stars.bigbang.repositories;

import com.stars.bigbang.entity.Plateform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlateformRepository extends JpaRepository<Plateform,Long> {
    Plateform findByPlateform(String plateform);
}
