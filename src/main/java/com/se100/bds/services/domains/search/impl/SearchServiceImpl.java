package com.se100.bds.services.domains.search.impl;

import com.se100.bds.models.schemas.report.PropertyStatisticsReport;
import com.se100.bds.models.schemas.search.SearchLog;
import com.se100.bds.repositories.domains.mongo.report.PropertyStatisticsReportRepository;
import com.se100.bds.repositories.domains.mongo.search.SearchLogRepository;
import com.se100.bds.services.domains.search.SearchService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchLogRepository searchLogRepository;
    private final PropertyStatisticsReportRepository propertyStatisticsReportRepository;

    @Override
    public void addSearch(UUID userId, UUID cityId, UUID districtId, UUID wardId, UUID propertyId, UUID propertyTypeId) {
        searchLogRepository.save(new SearchLog(userId, cityId, districtId, wardId, propertyId, propertyTypeId));
    }

    @Async
    @Override
    public void addSearchList(UUID userId, List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds, List<UUID> propertyTypeIds) {
        try {
            UUID cityId = (cityIds != null && !cityIds.isEmpty()) ? cityIds.get(0) : null;
            UUID districtId = (districtIds != null && !districtIds.isEmpty()) ? districtIds.get(0) : null;
            UUID wardId = (wardIds != null && !wardIds.isEmpty()) ? wardIds.get(0) : null;
            UUID propertyTypeId = (propertyTypeIds != null && !propertyTypeIds.isEmpty()) ? propertyTypeIds.get(0) : null;

            searchLogRepository.save(new SearchLog(userId, cityId, districtId, wardId, null, propertyTypeId));

            log.debug("Search log saved asynchronously for user: {}", userId);
        } catch (Exception e) {
            log.error("Error saving search log asynchronously: {}", e.getMessage());
        }
    }

    @Override
    public List<UUID> topKSearchByUser(UUID userId, int K, Constants.SearchTypeEnum searchType, int year, int month) {
        try {
            // Tìm PropertyStatisticsReport theo year và month
            Optional<PropertyStatisticsReport> reportOpt = propertyStatisticsReportRepository.findByYearAndMonth(year, month);

            if (reportOpt.isEmpty()) {
                log.warn("No PropertyStatisticsReport found for year {} and month {}", year, month);
                return List.of();
            }

            PropertyStatisticsReport report = reportOpt.get();

            // Lấy Map dữ liệu tương ứng với searchType
            Map<UUID, Integer> searchMap = getSearchMapByType(report, searchType, userId == null);

            if (searchMap == null || searchMap.isEmpty()) {
                return List.of();
            }

            // Sort theo count giảm dần và lấy top K
            return searchMap.entrySet().stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .limit(K)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding top {} searches for user {} with type {} in {}-{}: {}",
                    K, userId, searchType, year, month, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<UUID> getMostSearchedPropertyIds(int limit, int year, int month) {
        try {
            // Tìm PropertyStatisticsReport theo year và month
            Optional<PropertyStatisticsReport> reportOpt = propertyStatisticsReportRepository.findByYearAndMonth(year, month);

            if (reportOpt.isEmpty()) {
                log.warn("No PropertyStatisticsReport found for year {} and month {}", year, month);
                return List.of();
            }

            PropertyStatisticsReport report = reportOpt.get();
            Map<UUID, Integer> searchedProperties = report.getSearchedPropertiesMonth();

            if (searchedProperties == null || searchedProperties.isEmpty()) {
                return List.of();
            }

            // Sort theo count giảm dần và lấy top limit
            return searchedProperties.entrySet().stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting most searched property IDs with limit {} in {}-{}: {}",
                    limit, year, month, e.getMessage());
            return List.of();
        }
    }

    /**
     * Lấy Map dữ liệu search tương ứng với SearchTypeEnum
     * @param report PropertyStatisticsReport
     * @param searchType Loại search (CITY, DISTRICT, WARD, PROPERTY, PROPERTY_TYPE)
     * @param useMonthData true = dùng dữ liệu tháng hiện tại, false = dùng dữ liệu tích lũy
     * @return Map chứa UUID và số lần search
     */
    private Map<UUID, Integer> getSearchMapByType(PropertyStatisticsReport report,
                                                   Constants.SearchTypeEnum searchType,
                                                   boolean useMonthData) {
        return switch (searchType) {
            case CITY -> useMonthData ? report.getSearchedCitiesMonth() : report.getSearchedCities();
            case DISTRICT -> useMonthData ? report.getSearchedDistrictsMonth() : report.getSearchedDistricts();
            case WARD -> useMonthData ? report.getSearchedWardsMonth() : report.getSearchedWards();
            case PROPERTY -> useMonthData ? report.getSearchedPropertiesMonth() : report.getSearchedProperties();
            case PROPERTY_TYPE -> useMonthData ? report.getSearchedPropertyTypesMonth() : report.getSearchedPropertyTypes();
        };
    }
}
