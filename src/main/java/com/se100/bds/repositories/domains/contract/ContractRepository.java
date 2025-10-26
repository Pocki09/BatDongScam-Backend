package com.se100.bds.repositories.domains.contract;

import com.se100.bds.models.entities.contract.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {
    List<Contract> findAllByAgent_Id(UUID agentId);
    List<Contract> findAllByCustomer_Id(UUID customerId);
}

