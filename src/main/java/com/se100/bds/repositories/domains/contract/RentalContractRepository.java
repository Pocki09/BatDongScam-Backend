package com.se100.bds.repositories.domains.contract;

import com.se100.bds.models.entities.contract.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, UUID>, JpaSpecificationExecutor<RentalContract> {
}

