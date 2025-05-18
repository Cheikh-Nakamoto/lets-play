package com._talent.lets_play.config;

import com._talent.lets_play.models.User;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.services.impl.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor

public class JwtFilterToken extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserService userService;
    @Value("${admin.email}")
    private  String useradmin;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {  // Ajouté ServletException ici
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);  // Extrait le token sans "Bearer "
                System.out.println("Extracted token: " + token);

                // Vérifier d'abord si le token est valide
                if (jwtUtils.validateJwtToken(token)) {
                    // Si valide, alors récupérer le username
                    String username = jwtUtils.getUsernameFromJwtToken(token);
                    System.out.println("Username from token: " + username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                        UserPrincipal userDetails = useradmin.equals(username) ? new UserPrincipal(new User.Builder().build()) :(UserPrincipal) userService.loadUserByUsername(username) ;
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }

                    if (username != null) {
                        request.setAttribute("username", username);
                    }

                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        }
    }
}