package com.se100.bds.services.domains.location.impl;

import com.se100.bds.dtos.requests.location.CreateLocationRequest;
import com.se100.bds.dtos.requests.location.UpdateLocationRequest;
import com.se100.bds.dtos.responses.location.LocationCardResponse;
import com.se100.bds.dtos.responses.location.LocationDetailsResponse;
import com.se100.bds.mappers.LocationMapper;
import com.se100.bds.models.entities.location.City;
import com.se100.bds.models.entities.location.District;
import com.se100.bds.models.entities.location.Ward;
import com.se100.bds.repositories.domains.location.CityRepository;
import com.se100.bds.repositories.domains.location.DistrictRepository;
import com.se100.bds.repositories.domains.location.WardRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.services.domains.location.LocationService;
import com.se100.bds.services.domains.search.SearchService;
import com.se100.bds.utils.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PropertyRepository propertyRepository;
    private final SearchService searchService;
    private final LocationMapper locationMapper;


    @Override
    public Page<City> topMostSearchedCities(Pageable pageable) {
        // Lấy tháng và năm hiện tại
        int currentYear = java.time.LocalDateTime.now().getYear();
        int currentMonth = java.time.LocalDateTime.now().getMonthValue();

        // Lấy offset và limit từ Pageable
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        List<UUID> topCityIds = searchService.topMostSearchByUser(
                null,
                offset,
                limit,
                Constants.SearchTypeEnum.CITY,
                currentYear,
                currentMonth
        );
        return cityRepository.findAllByIdIn(topCityIds, pageable);
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

    @Override
    public Page<LocationCardResponse> findAllLocationCardsWithFilter(
            Pageable pageable,
            String keyWord,
            List<UUID> cityIds, List<UUID> districtIds,
            Constants.LocationEnum locationTypeEnum,
            Boolean isActive,
            BigDecimal minAvgLandPrice, BigDecimal maxAvgLandPrice,
            BigDecimal minArea, BigDecimal maxArea,
            Integer minPopulation, Integer maxPopulation
    ) {
        switch (locationTypeEnum) {
            case CITY -> {
                Page<City> cities = cityRepository.findAllWithFilters(
                        pageable,
                        keyWord,
                        cityIds,
                        isActive,
                        minAvgLandPrice, maxAvgLandPrice,
                        minArea, maxArea,
                        minPopulation, maxPopulation
                );
                return locationMapper.mapToPage(
                        cities, LocationCardResponse.class
                );
            }
            case DISTRICT -> {
                Page<District> districts = districtRepository.findAllWithFilters(
                        pageable,
                        keyWord,
                        cityIds,
                        districtIds,
                        isActive,
                        minAvgLandPrice, maxAvgLandPrice,
                        minArea, maxArea,
                        minPopulation, maxPopulation
                );
                return locationMapper.mapToPage(
                        districts, LocationCardResponse.class
                );
            }
            case WARD -> {
                Page<Ward> wards = wardRepository.findAllWithFilters(
                        pageable,
                        keyWord,
                        cityIds,
                        districtIds,
                        isActive,
                        minAvgLandPrice, maxAvgLandPrice,
                        minArea, maxArea,
                        minPopulation, maxPopulation
                );
                return locationMapper.mapToPage(
                        wards, LocationCardResponse.class
                );
            }
            default -> throw new IllegalArgumentException("Invalid locationTypeEnum");
        }
    }

    @Override
    public LocationDetailsResponse getLocationDetails(UUID locationId, Constants.LocationEnum locationTypeEnum) {
        LocationDetailsResponse locationDetailsResponse;

        switch (locationTypeEnum) {
            case CITY -> {
                City city = cityRepository.findById(locationId)
                        .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + locationId));
                locationDetailsResponse = locationMapper.mapTo(city, LocationDetailsResponse.class);

                locationDetailsResponse.setActiveProperties(propertyRepository.countActivePropertiesByCityId(city.getId()));
                locationDetailsResponse.setDistrictCount(cityRepository.countDistrictsByCityId(city.getId()));
                locationDetailsResponse.setWardCount(cityRepository.countWardsByCityId(city.getId()));
            }
            case DISTRICT -> {
                District district = districtRepository.findById(locationId)
                        .orElseThrow(() -> new EntityNotFoundException("District not found with id: " + locationId));
                locationDetailsResponse = locationMapper.mapTo(district, LocationDetailsResponse.class);

                locationDetailsResponse.setActiveProperties(propertyRepository.countActivePropertiesByDistrictId(district.getId()));
                locationDetailsResponse.setDistrictCount(0);
                locationDetailsResponse.setWardCount(districtRepository.countWardsByDistrictId(district.getId()));
            }
            case WARD -> {
                Ward ward = wardRepository.findById(locationId)
                        .orElseThrow(() -> new EntityNotFoundException("Ward not found with id: " + locationId));
                locationDetailsResponse = locationMapper.mapTo(ward, LocationDetailsResponse.class);

                locationDetailsResponse.setActiveProperties(propertyRepository.countActivePropertiesByWardId(ward.getId()));
                locationDetailsResponse.setDistrictCount(0);
                locationDetailsResponse.setWardCount(0);
            }
            default -> throw new IllegalArgumentException("Invalid locationTypeEnum");
        }

        return locationDetailsResponse;
    }

    @Override
    @Transactional
    public LocationCardResponse create(CreateLocationRequest createLocationRequest) {
        return null;
    }

    @Override
    @Transactional
    public LocationCardResponse update(UpdateLocationRequest updateLocationRequest) {
        return null;
    }

    @Override
    @Transactional
    public boolean delete(UUID locationId, Constants.LocationEnum locationTypeEnum) {
        return false;
    }
}
