package com._talent.lets_play.controllers;


import com._talent.lets_play.config.JwtUtils;
import com._talent.lets_play.exception.ErrorResponse;
import com._talent.lets_play.exception.UnauthorizedAccessException;
import com._talent.lets_play.models.*;
import com._talent.lets_play.services.IUser;
import com._talent.lets_play.utils.SecurityMaskingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
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
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token upon successful login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> authenticateUser(@Parameter(description = "User login credentials", required = true) @Valid @RequestBody LoginRequest loginRequest) {
        // Ne pas logger les identifiers complets dans les logs de production
        log.info("Authentication attempt for user: {}", SecurityMaskingUtils.maskUsername(loginRequest.getUsername()));


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

            // Set authentication in a security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwtToken = jwtUtils.generateJwtToken(userPrincipal);

            log.info("User authenticated successfully: {}", SecurityMaskingUtils.maskUsername(userPrincipal.getUsername()));

            // Return a successful response with token and user details
            return ResponseEntity.ok(new JwtResponse(jwtToken, userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail(), userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())));

        } catch (UsernameNotFoundException ex) {
            log.warn("Login failed - user not found: {}", SecurityMaskingUtils.maskUsername(loginRequest.getUsername()));

            // Ne pas révéler si c'est le nom d'utilisateur ou le mot de passe qui pose problème
           throw  new UnauthorizedAccessException("Nom d'utilisateur inexistant");
        } catch (BadCredentialsException ex) {
            log.warn("Login failed - bad credentials for user: {}", SecurityMaskingUtils.maskUsername(loginRequest.getUsername()));
            throw  new UnauthorizedAccessException("Nom d'utilisateur ou mot de passe invalide");
        } catch (InternalAuthenticationServiceException ex) {
            log.error("Authentication error", ex);

           throw new InternalAuthenticationServiceException("Une erreur est survenue lors de l'authentification");
        }
    }

    /**
     * Registers a new user.
     *
     * @param signupRequest The registration details
     * @return ResponseEntity with the created user or error message
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user account in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Email already in use or invalid data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> registerUser(@Parameter(description = "User registration details", required = true) @Valid @RequestBody SignupRequest signupRequest) {
        try {

            log.info("Processing user registration request for email: {}", signupRequest.getEmail());

            // Check if email is already in use
            if (userService.getUserbyEmail(signupRequest.getEmail()).isPresent()) {
                log.warn("Registration failed - email already in use: {}", signupRequest.getEmail());
                throw new com._talent.lets_play.exception.IncorrectResultSizeDataAccessException("Error during user registration, l'email existe deja");
            }

            // Create a new user
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

        } catch (IncorrectResultSizeDataAccessException ex) {
            log.error("Error during user registration", ex);
           // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred during registration");
            throw new com._talent.lets_play.exception.IncorrectResultSizeDataAccessException("Error during user registration");
        }
    }


}