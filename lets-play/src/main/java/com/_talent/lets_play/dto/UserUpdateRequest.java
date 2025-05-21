package com._talent.lets_play.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Field;


/**
 * DTO for user update requests.
 */
@Data
public class UserUpdateRequest {
    @Field
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String username;
    @Field
    @Email(message = "Email must be a valid email address")
    @Size(max = 50, message = "Email must be less than 50 characters")
    private String email;
    @Field
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}