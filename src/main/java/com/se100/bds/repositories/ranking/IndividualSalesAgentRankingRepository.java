package com.se100.bds.repositories.ranking;

import com.se100.bds.entities.ranking.IndividualSalesAgentRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IndividualSalesAgentRankingRepository extends JpaRepository<IndividualSalesAgentRanking, UUID>, JpaSpecificationExecutor<IndividualSalesAgentRanking> {
}

