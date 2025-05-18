// Création d'une classe de configuration pour instancier le Builder

package com._talent.lets_play.config;

import com._talent.lets_play.models.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminConfig {
    @Bean
    public User.Builder adminBuilder() {
        return new User.Builder();
    }
}