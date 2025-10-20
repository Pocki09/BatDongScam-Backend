package com.se100.bds.dtos.requests.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RegisterRequest {

    @NotBlank(message = "{not_blank}")
    @Size(max = 50, message = "{max_length}")
    @Schema(
            name = "firstName",
            description = "First name of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Minh"
    )
    private String firstName;

    @NotBlank(message = "{not_blank}")
    @Size(max = 50, message = "{max_length}")
    @Schema(
            name = "lastName",
            description = "Last name of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Phan Dinh"
    )
    private String lastName;

    @NotBlank(message = "{not_blank}")
    @Email(message = "{invalid_email}")
    @Size(max = 100, message = "{max_length}")
    @Schema(
            name = "email",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "mail@email.com"
    )
    private String email;

    @Size(max = 20, message = "{max_length}")
    @Schema(
            name = "phoneNumber",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "0901234567"
    )
    private String phoneNumber;

    @Schema(
            name = "wardId",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID wardId;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "password",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String password;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "passwordConfirm",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String passwordConfirm;

}