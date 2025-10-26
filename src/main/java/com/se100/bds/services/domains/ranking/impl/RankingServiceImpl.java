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
                var ranking = individualCustomerPotentialMonthRepository.findByCustomerIdAndMonthAndYear(
                        userId, month, year
                );
                return ranking != null && ranking.getCustomerTier() != null
                        ? ranking.getCustomerTier().name()
                        : null;
            }
            case SALESAGENT -> {
                var ranking = individualSalesAgentPerformanceMonthRepository.findByAgentIdAndMonthAndYear(
                        userId, month, year
                );
                return ranking != null && ranking.getPerformanceTier() != null
                        ? ranking.getPerformanceTier().name()
                        : null;
            }
            case PROPERTY_OWNER -> {
                var ranking = individualPropertyOwnerContributionMonthRepository.findByOwnerIdAndMonthAndYear(
                        userId, month, year
                );
                return ranking != null && ranking.getContributionTier() != null
                        ? ranking.getContributionTier().name()
                        : null;
            }
            default -> {
                return null;
            }
        }
    }
}
