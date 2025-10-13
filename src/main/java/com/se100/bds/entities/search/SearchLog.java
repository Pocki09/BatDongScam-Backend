package com.se100.bds.entities.search;

import com.se100.bds.entities.AbstractMongoBaseEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "search_logs")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog extends AbstractMongoBaseEntity {
    @Field("user_id")
    private UUID userId;

    @Field("city_id")
    private UUID cityId;

    @Field("district_id")
    private UUID districtId;

    @Field("ward_id")
    private UUID wardId;

    @Field("property_id")
    private UUID propertyId;

    @Field("property_type_id")
    private UUID propertyTypeId;
}
