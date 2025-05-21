package com._talent.lets_play.utils;

import com._talent.lets_play.models.User;

import java.util.HashMap;
import java.util.Map;

import static com._talent.lets_play.utils.SecurityMaskingUtils.maskUsername;

public class MakeResponse {
    public static Map<String, Object> makeresponse(User savedUser) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("email", maskUsername(savedUser.getEmail()));
        response.put("username", maskUsername(savedUser.getName()));
        response.put("role", savedUser.getRole());
        return response;
    }
}
