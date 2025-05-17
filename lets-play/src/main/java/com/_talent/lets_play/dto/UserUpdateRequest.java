package com._talent.lets_play.dto;

import lombok.Data;
import lombok.Setter;

/**
 * DTO for user update requests.
 */
@Data
public class UserUpdateRequest {
    private String name;
    private String password;
}