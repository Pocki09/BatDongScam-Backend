package com.se100.bds.services.domains.location.impl;

import com.se100.bds.models.entities.location.City;
import com.se100.bds.models.entities.location.District;
import com.se100.bds.models.entities.location.Ward;
import com.se100.bds.repositories.domains.location.CityRepository;
import com.se100.bds.repositories.domains.location.DistrictRepository;
import com.se100.bds.repositories.domains.location.WardRepository;
import com.se100.bds.services.domains.location.LocationService;
import com.se100.bds.services.domains.search.SearchService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final SearchService searchService;


    @Override
    public Page<City> topKCities(Pageable pageable, int topK) {
        // Lấy tháng và năm hiện tại
        int currentYear = java.time.LocalDateTime.now().getYear();
        int currentMonth = java.time.LocalDateTime.now().getMonthValue();

        List<UUID> topKCityIds = searchService.topKSearchByUser(null, topK, Constants.SearchTypeEnum.CITY, currentYear, currentMonth);
        return cityRepository.findAllByIdIn(topKCityIds, pageable);
    }

    @Override
    public Map<UUID, String> findAllByParents(UUID parentId, Constants.SearchTypeEnum searchTypeEnum) {
        Map<UUID, String> results = new HashMap<>();

        switch (searchTypeEnum) {
            case CITY -> {
                List<City> cities = cityRepository.findAll();
                for  (City city : cities) {
                    results.put(city.getId(), city.getCityName());
                }
            }
            case DISTRICT -> {
                List<District> districts = districtRepository.findAllByCity_Id(parentId);
                for  (District district : districts) {
                    results.put(district.getId(), district.getDistrictName());
                }
            }
            case WARD -> {
                List<Ward> wards = wardRepository.findAllByDistrict_Id(parentId);
                for  (Ward ward : wards) {
                    results.put(ward.getId(), ward.getWardName());
                }
            }
            default -> throw new IllegalArgumentException("Invalid searchTypeEnum");
        }

        return results;
    }
}
