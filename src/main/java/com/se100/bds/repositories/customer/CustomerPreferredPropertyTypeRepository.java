package com.se100.bds.repositories.customer;

import com.se100.bds.entities.customer.CustomerPreferredPropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerPreferredPropertyTypeRepository extends JpaRepository<CustomerPreferredPropertyType, UUID>, JpaSpecificationExecutor<CustomerPreferredPropertyType> {
}

