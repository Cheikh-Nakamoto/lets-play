package com._talent.lets_play.controllers;


import com._talent.lets_play.config.JwtUtils;
import com._talent.lets_play.exception.ErrorResponse;
import com._talent.lets_play.models.*;
import com._talent.lets_play.services.IUser;
import com._talent.lets_play.services.impl.UserService;
import com._talent.lets_play.utils.SecurityMaskingUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com._talent.lets_play.utils.MakeResponse.makeresponse;

/**
 * Controller handling authentication and user management operations.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthControllers {

    private final IUser userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final User.Builder admin;


    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginRequest The login credentials
     * @return ResponseEntity with JWT token or error message
     */

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Ne pas logger les identifiants complets dans les logs de production
        log.info("Authentication attempt for user: {}", SecurityMaskingUtils.maskUsername(loginRequest.getUsername()));

        String path = "/api/auth";
        ErrorResponse.Builder builder = new ErrorResponse.Builder().withCode("VALIDATION_ERROR").withStatus(HttpStatus.BAD_REQUEST.value()).withTimestamp(LocalDateTime.now()).withPath(String.join("/", path, "login"));

        try {
            // Vérifications préliminaires des entrées
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                throw new BadCredentialsException("Username and password are required");
            }

            Authentication authentication;
            UserPrincipal userPrincipal;
            // Vérification pour l'utilisateur admin spécial en utilisant les propriétés de configuration
            if ((admin.getAdminEmail().equals(loginRequest.getUsername()) || admin.getAdminUsername().equals(loginRequest.getUsername())) && admin.getAdminPassword().equals(loginRequest.getPassword())) {
                log.info("Admin special account authentication attempt");


                userPrincipal = new UserPrincipal(admin.build());

                // Création manuelle de l'authentification
                authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null);

                log.info("Admin special account authenticated successfully");
            } else {
                // Authentification standard pour tous les autres utilisateurs
                authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

                // Get user details
                userPrincipal = (UserPrincipal) authentication.getPrincipal();
            }

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwtToken = jwtUtils.generateJwtToken(userPrincipal);

            log.info("User authenticated successfully: {}", SecurityMaskingUtils.maskUsername(userPrincipal.getUsername()));

            // Return successful response with token and user details
            return ResponseEntity.ok(new JwtResponse(jwtToken, userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail(), userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())));

        } catch (UsernameNotFoundException ex) {
            log.warn("Login failed - user not found: {}", SecurityMaskingUtils.maskUsername(loginRequest.getUsername()));
            builder.withMessage("Nom d'utilisateur ou mot de passe invalide");
            // Ne pas révéler si c'est le nom d'utilisateur ou le mot de passe qui pose problème
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(builder.build());
        } catch (BadCredentialsException ex) {
            log.warn("Login failed - bad credentials for user: {}", SecurityMaskingUtils.maskUsername(loginRequest.getUsername()));
            builder.withMessage("Nom d'utilisateur ou mot de passe invalide");
            // Ne pas révéler si c'est le nom d'utilisateur ou le mot de passe qui pose problème
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(builder.build());
        } catch (Exception ex) {
            log.error("Authentication error", ex);
            builder.withMessage("Une erreur est survenue lors de l'authentification");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(builder.build());
        }
    }

    /**
     * Registers a new user.
     *
     * @param signupRequest The registration details
     * @return ResponseEntity with the created user or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {

            log.info("Processing user registration request for email: {}", signupRequest.getEmail());

            // Check if email is already in use
            if (userService.getUserbyEmail(signupRequest.getEmail()).isPresent()) {
                log.warn("Registration failed - email already in use: {}", signupRequest.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email address is already in use");
            }

            // Create new user
            User user = new User();
            user.setEmail(signupRequest.getEmail());
            user.setName(signupRequest.getUsername());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setRole("USER"); // Default role

            // Save user
            User savedUser = userService.addUser(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());

            // Return success response without exposing password


            return ResponseEntity.status(HttpStatus.CREATED).body(makeresponse(savedUser));

        } catch (Exception ex) {
            log.error("Error during user registration", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred during registration");
        }
    }


}