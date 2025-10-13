package com.se100.bds.services.domains.search.impl;

import com.se100.bds.entities.search.SearchLog;
import com.se100.bds.repositories.domains.search.SearchLogRepository;
import com.se100.bds.services.domains.search.SearchService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchLogRepository searchLogRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void addSearch(UUID userId, UUID cityId, UUID districtId, UUID wardId, UUID propertyId, UUID propertyTypeId) {
        searchLogRepository.save(new SearchLog(userId, cityId, districtId, wardId, propertyId, propertyTypeId));
    }

    @Async
    @Override
    public void addSearchList(UUID userId, List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds, List<UUID> propertyTypeIds) {
        try {
            // Lấy giá trị đầu tiên từ mỗi list (nếu có)
            UUID cityId = (cityIds != null && !cityIds.isEmpty()) ? cityIds.get(0) : null;
            UUID districtId = (districtIds != null && !districtIds.isEmpty()) ? districtIds.get(0) : null;
            UUID wardId = (wardIds != null && !wardIds.isEmpty()) ? wardIds.get(0) : null;
            UUID propertyTypeId = (propertyTypeIds != null && !propertyTypeIds.isEmpty()) ? propertyTypeIds.get(0) : null;

            // Lưu search log
            searchLogRepository.save(new SearchLog(userId, cityId, districtId, wardId, null, propertyTypeId));

            log.debug("Search log saved asynchronously for user: {}", userId);
        } catch (Exception e) {
            log.error("Error saving search log asynchronously: {}", e.getMessage());
        }
    }

    @Override
    public List<UUID> topKSearchByUser(UUID userId, int K, Constants.SearchTypeEnum searchType) {
        try {
            // Xác định field name dựa trên searchType
            String fieldName = getFieldNameBySearchType(searchType);

            // Tạo aggregation pipeline
            Aggregation aggregation;

            if (userId == null) {
                // Top K search trên toàn hệ thống
                aggregation = Aggregation.newAggregation(
                        // Match: chỉ lấy documents có field không null
                        Aggregation.match(Criteria.where(fieldName).ne(null)),
                        // Group by field và đếm số lần xuất hiện
                        Aggregation.group(fieldName).count().as("count"),
                        // Sort theo count giảm dần
                        Aggregation.sort(Sort.Direction.DESC, "count"),
                        // Limit K kết quả
                        Aggregation.limit(K)
                );
            } else {
                // Top K search của user cụ thể
                aggregation = Aggregation.newAggregation(
                        // Match: lọc theo userId và field không null
                        Aggregation.match(
                                Criteria.where("user_id").is(userId)
                                        .and(fieldName).ne(null)
                        ),
                        // Group by field và đếm số lần xuất hiện
                        Aggregation.group(fieldName).count().as("count"),
                        // Sort theo count giảm dần
                        Aggregation.sort(Sort.Direction.DESC, "count"),
                        // Limit K kết quả
                        Aggregation.limit(K)
                );
            }

            // Execute aggregation
            AggregationResults<SearchAggregationResult> results = mongoTemplate.aggregate(
                    aggregation,
                    "search_logs",
                    SearchAggregationResult.class
            );

            // Extract UUIDs từ kết quả
            return results.getMappedResults().stream()
                    .map(result -> UUID.fromString(result.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding top {} searches for user {} with type {}: {}",
                    K, userId, searchType, e.getMessage());
            return List.of();
        }
    }

    /**
     * Map SearchTypeEnum to MongoDB field name
     */
    private String getFieldNameBySearchType(Constants.SearchTypeEnum searchType) {
        return switch (searchType) {
            case PROPERTY -> "property_id";
            case CITY -> "city_id";
            case DISTRICT -> "district_id";
            case WARD -> "ward_id";
            case PROPERTY_TYPE -> "property_type_id";
        };
    }

    /**
     * DTO class để nhận kết quả aggregation từ MongoDB
     */
    @lombok.Data
    private static class SearchAggregationResult {
        private String id;  // Đây sẽ là UUID dưới dạng String
        private long count;
    }
}
