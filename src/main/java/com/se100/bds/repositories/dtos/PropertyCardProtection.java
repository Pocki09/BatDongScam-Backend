package com.se100.bds.repositories.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PropertyCardProtection {
    private final UUID id;
    private final String title;
    private final String thumbnailUrl;
    private final boolean isFavorite;
    private final int numberOfImages;
    private final String address;
    private final String district;
    private final String city;
    private final String status;
    private final BigDecimal price;
    private final BigDecimal totalArea;
}
