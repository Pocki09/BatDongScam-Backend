package com.se100.bds.services.dtos.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PropertyCard {
    private UUID id;
    private String title;
    private String thumbnailUrl;
    private boolean isFavorite;
    private int numberOfImages;
    private String district;
    private String city;
    private String status;
    private BigDecimal price;
    private BigDecimal totalArea;
}
