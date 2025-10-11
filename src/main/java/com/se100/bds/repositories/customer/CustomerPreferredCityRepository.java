package com.se100.bds.repositories.customer;

import com.se100.bds.entities.customer.CustomerPreferredCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerPreferredCityRepository extends JpaRepository<CustomerPreferredCity, UUID>, JpaSpecificationExecutor<CustomerPreferredCity> {
}

