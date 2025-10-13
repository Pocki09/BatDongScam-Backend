package com.se100.bds.services.domains.customer;

import com.se100.bds.utils.Constants;

import java.util.UUID;

public interface CustomerFavoriteService {
    boolean like(UUID refId, Constants.LikeTypeEnum likeType);
    boolean isLike(UUID refId, UUID customerId, Constants.LikeTypeEnum likeType);
}
