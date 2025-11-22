package com.se100.bds.services.domains.ranking.impl;

import com.se100.bds.models.schemas.ranking.*;
import com.se100.bds.repositories.domains.mongo.ranking.*;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final UserService userService;

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

    @Override
    public String getCurrentTier(UUID userId, Constants.RoleEnum role) {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();
        return getTier(userId, role, month, year);
    }

    @Override
    public IndividualSalesAgentPerformanceMonth getSaleAgentMonth(UUID agentId, int month, int year) {
        return individualSalesAgentPerformanceMonthRepository.findByAgentIdAndMonthAndYear(
                agentId, month, year
        );
    }

    @Override
    public IndividualSalesAgentPerformanceMonth getMySaleAgentMonth(int month, int year) {
        return getSaleAgentMonth(userService.getUserId(), month, year);
    }

    @Override
    public Page<IndividualSalesAgentPerformanceMonth> getSaleAgentMonths(Pageable pageable) {
        return null;
    }

    @Override
    public IndividualSalesAgentPerformanceMonth getSaleAgentCurrentMonth(UUID agentId) {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();

        return getSaleAgentMonth(agentId, month, year);
    }

    @Override
    public IndividualSalesAgentPerformanceCareer getSaleAgentCareer(UUID agentId) {
        return individualSalesAgentPerformanceCareerRepository.findByAgentId(agentId);
    }

    @Override
    public IndividualCustomerPotentialMonth getCustomerMonth(UUID customerId, int month, int year) {
        return individualCustomerPotentialMonthRepository.findByCustomerIdAndMonthAndYear(
                customerId, month, year
        );
    }

    @Override
    public IndividualCustomerPotentialMonth getCustomerCurrentMonth(UUID customerId) {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();

        return getCustomerMonth(customerId, month, year);
    }

    @Override
    public IndividualCustomerPotentialAll getCustomerAll(UUID customerId) {
        return individualCustomerPotentialAllRepository.findByCustomerId(customerId);
    }

    @Override
    public IndividualPropertyOwnerContributionMonth getPropertyOwnerMonth(UUID propertyOwnerId, int month, int year) {
        return individualPropertyOwnerContributionMonthRepository.findByOwnerIdAndMonthAndYear(
                propertyOwnerId, month, year
        );
    }

    @Override
    public IndividualPropertyOwnerContributionMonth getPropertyOwnerCurrentMonth(UUID propertyOwnerId) {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();
        return getPropertyOwnerMonth(propertyOwnerId, month, year);
    }

    @Override
    public IndividualPropertyOwnerContributionAll getPropertyOwnerAll(UUID propertyOwnerId) {
        return individualPropertyOwnerContributionAllRepository.findByOwnerId(propertyOwnerId);
    }
}
