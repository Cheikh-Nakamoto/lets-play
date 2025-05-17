/*
package com._talent.lets_play.config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.util.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/products/**");
    }

    public static class RateLimitInterceptor implements HandlerInterceptor {
        // Limiteur de débit global : 100 requêtes par seconde
        private final RateLimiter globalRateLimiter = RateLimiter.create(100.0);

        // Limiteur de débit par IP : 10 requêtes par seconde maximum
        private final Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // Obtenir l'adresse IP du client
            String clientIp = getClientIp(request);

            // Vérifier le limiteur global
            if (!globalRateLimiter.tryAcquire()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Trop de requêtes, veuillez réessayer plus tard");
                return false;
            }

            // Obtenir ou créer un limiteur pour cette IP
            RateLimiter ipLimiter = ipRateLimiters.computeIfAbsent(clientIp, k -> RateLimiter.create(10.0));

            // Vérifier le limiteur par IP
            if (!ipLimiter.tryAcquire()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Trop de requêtes depuis votre adresse IP, veuillez réessayer plus tard");
                return false;
            }

            return true;
        }

        private String getClientIp(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
    }
}*/
