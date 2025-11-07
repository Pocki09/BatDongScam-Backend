package com.se100.bds.services.domains.location;

import com.se100.bds.dtos.requests.location.CreateLocationRequest;
import com.se100.bds.dtos.requests.location.UpdateLocationRequest;
import com.se100.bds.dtos.responses.location.LocationCardResponse;
import com.se100.bds.dtos.responses.location.LocationDetailsResponse;
import com.se100.bds.models.entities.location.City;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LocationService {
    // public
    Page<City> topMostSearchedCities(Pageable pageable);
    Map<UUID, String> findAllByParents(UUID parentId, Constants.SearchTypeEnum searchTypeEnum);
    Page<LocationCardResponse> findAllLocationCardsWithFilter(
            Pageable pageable,
            String keyWord,
            List<UUID> cityIds, List<UUID> districtIds,
            Constants.LocationEnum locationTypeEnum,
            Boolean isActive,
            BigDecimal minAvgLandPrice, BigDecimal maxAvgLandPrice,
            BigDecimal minArea, BigDecimal maxArea,
            Integer minPopulation, Integer maxPopulation
    );
    LocationDetailsResponse getLocationDetails(UUID locationId, Constants.LocationEnum locationTypeEnum);
    LocationCardResponse create(CreateLocationRequest createLocationRequest);
    LocationCardResponse update(UpdateLocationRequest updateLocationRequest);
    boolean delete(UUID locationId, Constants.LocationEnum locationTypeEnum);
}
