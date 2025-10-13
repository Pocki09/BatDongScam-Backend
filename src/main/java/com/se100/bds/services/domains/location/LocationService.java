package com.se100.bds.services.domains.location;

import com.se100.bds.entities.location.City;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LocationService {
    Page<City> topKCities(Pageable pageable, int topK);
    Map<UUID, String> findAllByParents(UUID parentId, Constants.SearchTypeEnum searchTypeEnum);
}
