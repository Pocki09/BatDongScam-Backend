package com.se100.bds.repositories.domains.contract;

import com.se100.bds.models.entities.contract.PurchaseContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurchaseContractRepository extends JpaRepository<PurchaseContract, UUID>, JpaSpecificationExecutor<PurchaseContract> {
}

