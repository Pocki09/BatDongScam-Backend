package com.se100.bds.services.domains.ranking;

import com.se100.bds.models.schemas.ranking.*;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RankingService {
    String getTier(UUID userId, Constants.RoleEnum role, int month, int year);
    String getCurrentTier(UUID userId, Constants.RoleEnum role);
    IndividualSalesAgentPerformanceMonth getSaleAgentCurrentMonth(UUID agentId);
    IndividualCustomerPotentialMonth getCustomerCurrentMonth(UUID customerId);
    IndividualPropertyOwnerContributionMonth getPropertyOwnerCurrentMonth(UUID propertyOwnerId);


    IndividualSalesAgentPerformanceMonth getSaleAgentMonth(UUID agentId, int month, int year);
    IndividualSalesAgentPerformanceMonth getMySaleAgentMonth(int month, int year);
    Page<IndividualSalesAgentPerformanceMonth> getSaleAgentMonths(Pageable pageable);
    IndividualSalesAgentPerformanceCareer getSaleAgentCareer(UUID agentId);
    IndividualCustomerPotentialMonth getCustomerMonth(UUID customerId, int month, int year);
    IndividualCustomerPotentialAll getCustomerAll(UUID customerId);
    IndividualPropertyOwnerContributionMonth getPropertyOwnerMonth(UUID propertyOwnerId, int month, int year);
    IndividualPropertyOwnerContributionAll getPropertyOwnerAll(UUID propertyOwnerId);


    // TODO: Action methods
}
