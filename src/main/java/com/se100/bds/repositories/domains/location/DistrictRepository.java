package com.se100.bds.repositories.domains.location;

import com.se100.bds.models.entities.location.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<District, UUID>, JpaSpecificationExecutor<District> {
    List<District> findAllByCity_Id(UUID cityId);
}

