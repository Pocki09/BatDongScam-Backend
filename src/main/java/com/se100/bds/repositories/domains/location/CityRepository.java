package com.se100.bds.repositories.domains.location;

import com.se100.bds.models.entities.location.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID>, JpaSpecificationExecutor<City> {
    Page<City> findAllByIdIn(Collection<UUID> ids, Pageable pageable);
}

