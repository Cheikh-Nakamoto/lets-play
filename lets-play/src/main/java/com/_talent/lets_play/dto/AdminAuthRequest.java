package com._talent.lets_play.dto;

import lombok.Data;

/**
 * DTO for admin authentication requests.
 */
@Data
public class AdminAuthRequest {
    private String username;
    private String password;
}