package com.se100.bds.dtos.responses.user;

import com.se100.bds.dtos.responses.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserProfileResponse<T> extends AbstractBaseDataResponse {
    private String role;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String zaloContact;
    private String status;
    private T propertyProfile;
}
