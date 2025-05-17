package com._talent.lets_play.config;
import com._talent.lets_play.models.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;



@Component
@RequiredArgsConstructor
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private String jwtExpirationMs;

    SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateJwtToken(UserPrincipal authentication) {
        System.out.println("JWT secret: " + jwtExpirationMs);
        int expiration = Integer.parseInt(jwtExpirationMs);
        return Jwts.builder()
                .subject(authentication.getEmail())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith( getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public boolean validateJwtToken(String authToken) {
        try {

            Jwts.parser().verifyWith( getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
           System.out.println("Token JWT invalide: " + e.getMessage());
        } catch (ExpiredJwtException e) {
           System.out.println("Token JWT expiré: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
           System.out.println("Token JWT non supporté: " + e.getMessage());
        } catch (IllegalArgumentException e) {
           System.out.println("La chaîne claims JWT est vide: " + e.getMessage());
        }
        return false;
    }
}
