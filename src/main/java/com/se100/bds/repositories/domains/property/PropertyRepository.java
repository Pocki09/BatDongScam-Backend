package com.se100.bds.repositories.domains.property;

import com.se100.bds.models.entities.property.Property;
import com.se100.bds.repositories.dtos.PropertyCardProtection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {
    @Query("""
        SELECT new com.se100.bds.repositories.dtos.PropertyCardProtection (
            p.id,
            p.title,
            (SELECT m.filePath FROM Media m WHERE m.property.id = p.id ORDER BY m.createdAt ASC LIMIT 1),
            false,
            COALESCE((SELECT CAST(COUNT(m.id) AS int) FROM Media m WHERE m.property.id = p.id), 0),
            p.fullAddress,
            d.districtName,
            c.cityName,
            CAST(p.status AS string),
            p.priceAmount,
            p.area
        )
        FROM Property p
        JOIN Ward w ON p.ward.id = w.id
        JOIN District d ON w.district.id = d.id
        JOIN City c ON d.city.id = c.id
    """)
    Page<PropertyCardProtection> findAllPropertyCards(Pageable pageable);

    @Query("""
    SELECT new com.se100.bds.repositories.dtos.PropertyCardProtection (
        p.id,
        p.title,
        (SELECT m2.filePath FROM Media m2 WHERE m2.property.id = p.id ORDER BY m2.createdAt ASC LIMIT 1),
        false,
        COALESCE((SELECT CAST(COUNT(m2.id) AS int) FROM Media m2 WHERE m2.property.id = p.id), 0),
        p.fullAddress,
        d.districtName,
        c.cityName,
        CAST(p.status AS string),
        p.priceAmount,
        p.area
    )
    FROM Property p
    JOIN Ward w ON p.ward.id = w.id
    JOIN District d ON w.district.id = d.id
    JOIN City c ON d.city.id = c.id
    WHERE
        (COALESCE(:cityIds, NULL) IS NULL OR c.id IN :cityIds)
        AND (COALESCE(:districtIds, NULL) IS NULL OR d.id IN :districtIds)
        AND (COALESCE(:wardIds, NULL) IS NULL OR w.id IN :wardIds)
        AND (COALESCE(:propertyTypeIds, NULL) IS NULL OR p.propertyType.id IN :propertyTypeIds)
        AND (:minPrice IS NULL OR p.priceAmount >= :minPrice)
        AND (:maxPrice IS NULL OR p.priceAmount <= :maxPrice)
        AND (:totalArea IS NULL OR p.area >= :totalArea)
        AND (:rooms IS NULL OR p.rooms = :rooms)
        AND (:bathrooms IS NULL OR p.bathrooms = :bathrooms)
        AND (:bedrooms IS NULL OR p.bedrooms = :bedrooms)
        AND (:floors IS NULL OR p.floors = :floors)
        AND (:houseOrientation IS NULL OR p.houseOrientation = :houseOrientation)
        AND (:balconyOrientation IS NULL OR p.balconyOrientation = :balconyOrientation)
        AND (:transactionType IS NULL OR p.transactionType = :transactionType)
    """)
    Page<PropertyCardProtection> findAllPropertyCardsWithFilter(
            Pageable pageable,
            @Param("cityIds") List<UUID> cityIds,
            @Param("districtIds") List<UUID> districtIds,
            @Param("wardIds") List<UUID> wardIds,
            @Param("propertyTypeIds") List<UUID> propertyTypeIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("totalArea") BigDecimal totalArea,
            @Param("rooms") Integer rooms,
            @Param("bathrooms") Integer bathrooms,
            @Param("bedrooms") Integer bedrooms,
            @Param("floors") Integer floors,
            @Param("houseOrientation") String houseOrientation,
            @Param("balconyOrientation") String balconyOrientation,
            @Param("transactionType") String transactionType,
            @Param("userId") UUID userId
    );
}