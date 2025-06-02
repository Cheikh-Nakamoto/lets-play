package com._talent.lets_play.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Field;


/**
 * DTO for user update requests.
 */
@Data
public class UserUpdateRequest {
    @Schema(
            description = "New username for the user",
            example = "john_doe_2024",
            minLength = 2,
            maxLength = 100,
            nullable = true
    )
    @Field
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String username;

    @Schema(
            description = "New password for the user",
            example = "newSecurePassword123!",
            minLength = 8,
            maxLength = 100,
            nullable = true,
            format = "password"
    )
    @Field
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

}
