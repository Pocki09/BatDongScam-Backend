package com.se100.bds.entities.report;

import com.se100.bds.entities.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PropertyStatisticsReport")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyStatisticsReport extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "report_id", referencedColumnName = "report_id")
    private Report report;

    @Column(name = "total_active_properties")
    private Integer totalActiveProperties;

    @Column(name = "total_sold_properties_current_month")
    private Integer totalSoldPropertiesCurrentMonth;

    @Column(name = "total_sold_properties", precision = 5, scale = 2)
    private BigDecimal totalSoldProperties;

    @Column(name = "total_rented_properties_current_month", precision = 15, scale = 2)
    private BigDecimal totalRentedPropertiesCurrentMonth;

    @Column(name = "total_rented_properties")
    private Integer totalRentedProperties;

    @Column(name = "most_popular_property_type_id")
    private String mostPopularPropertyTypeId;

    @Column(name = "most_popular_location_id")
    private String mostPopularLocationId;
}

