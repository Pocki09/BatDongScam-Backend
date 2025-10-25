package com.se100.bds.models.schemas.report;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "property_statistic_reports")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyStatisticsReport extends AbstractBaseMongoReport {
    @Field("total_active_properties")
    private Integer totalActiveProperties;

    @Field("new_this_month")
    private Integer newThisMonth;

    @Field("total_sold_properties_current_month")
    private Integer totalSoldPropertiesCurrentMonth;

    @Field("total_sold_properties")
    private Integer totalSoldPropertiesCurrentDay;

    @Field("total_rented_properties_current_month")
    private Integer totalRentedPropertiesCurrentMonth;

    @Field("total_rented_properties")
    private Integer totalRentedPropertiesCurrentDay;

    @Field("searched_cities_month")
    private List<RankedItem> searchedCitiesMonth;

    @Field("searched_cities")
    private List<RankedItem> searchedCities;

    @Field("favorite_cities")
    private List<RankedItem> favoriteCities;

    @Field("searched_districts_month")
    private List<RankedItem> searchedDistrictsMonth;

    @Field("searched_districts")
    private List<RankedItem> searchedDistricts;

    @Field("favorite_districts")
    private List<RankedItem> favoriteDistricts;

    @Field("searched_wards_month")
    private List<RankedItem> searchedWardsMonth;

    @Field("searched_wards")
    private List<RankedItem> searchedWards;

    @Field("favorite_wards")
    private List<RankedItem> favoriteWards;

    @Field("searched_property_types_month")
    private List<RankedItem> searchedPropertyTypesMonth;

    @Field("searched_property_types")
    private List<RankedItem> searchedPropertyTypes;

    @Field("favorite_property_types")
    private List<RankedItem> favoritePropertyTypes;

    @Field("searched_properties_month")
    private List<RankedItem> searchedPropertiesMonth;

    @Field("searched_properties")
    private List<RankedItem> searchedProperties;
}
