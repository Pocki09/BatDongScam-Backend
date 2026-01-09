package com.se100.bds.services.domains.contract.impl;

import com.se100.bds.dtos.requests.contract.CreateContractRequest;
import com.se100.bds.dtos.requests.contract.CreateDepositContractRequest;
import com.se100.bds.dtos.responses.contract.CreateContractResponse;
import com.se100.bds.services.domains.contract.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    // TODO
    @Override
    public CreateContractResponse createContract(CreateContractRequest request) {
        return null;
    }

    @Override
    public CreateContractResponse createDepositContract(CreateDepositContractRequest request) {
        return null;
    }
}
