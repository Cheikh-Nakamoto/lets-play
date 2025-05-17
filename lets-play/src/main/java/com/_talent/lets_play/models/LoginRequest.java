package com._talent.lets_play.models;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}