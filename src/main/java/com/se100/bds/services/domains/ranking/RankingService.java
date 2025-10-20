package com.se100.bds.services.domains.ranking;

import com.se100.bds.utils.Constants;

import java.util.UUID;

public interface RankingService {
    public String getTier(UUID userId, Constants.RoleEnum role, int month, int year);
}
