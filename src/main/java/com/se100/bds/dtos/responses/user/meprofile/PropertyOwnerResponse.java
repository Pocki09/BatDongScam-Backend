package com.se100.bds.dtos.responses.user.meprofile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PropertyOwnerResponse {
    private String identificationNumber;
    private int forRent;
    private int forSale;
    private int renting;
    private int sold;
    private LocalDateTime approvedAt;
}
