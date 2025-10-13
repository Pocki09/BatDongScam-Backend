package com.se100.bds.repositories.domains.location;

import com.se100.bds.entities.location.Ward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID>, JpaSpecificationExecutor<Ward> {
    List<Ward> findAllByDistrict_Id(UUID districtId);
}
