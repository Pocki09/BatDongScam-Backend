package com.se100.bds.services.domains.ranking.impl;

import com.se100.bds.repositories.domains.mongo.ranking.*;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {
    private final IndividualCustomerPotentialAllRepository individualCustomerPotentialAllRepository;
    private final IndividualCustomerPotentialMonthRepository  individualCustomerPotentialMonthRepository;
    private final IndividualSalesAgentPerformanceCareerRepository individualSalesAgentPerformanceCareerRepository;
    private final IndividualSalesAgentPerformanceMonthRepository individualSalesAgentPerformanceMonthRepository;
    private final IndividualPropertyOwnerContributionAllRepository individualPropertyOwnerContributionAllRepository;
    private final IndividualPropertyOwnerContributionMonthRepository individualPropertyOwnerContributionMonthRepository;

    @Override
    public String getTier(UUID userId, Constants.RoleEnum role, int month, int year) {
        switch(role){
            case CUSTOMER -> {
                return individualCustomerPotentialMonthRepository.findByCustomerIdAndMonthAndYear(
                        userId, month, year
                ).getCustomerTier().name();
            }
            case SALESAGENT -> {
                return individualSalesAgentPerformanceMonthRepository.findByAgentIdAndMonthAndYear(
                        userId, month, year
                ).getPerformanceTier().name();
            }
            case PROPERTY_OWNER -> {
                return individualPropertyOwnerContributionMonthRepository.findByOwnerIdAndMonthAndYear(
                        userId, month, year
                ).getContributionTier().name();
            }
            default -> {
                return null;
            }
        }
    }
}
