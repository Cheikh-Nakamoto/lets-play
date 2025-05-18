package com._talent.lets_play.controllers;

import com._talent.lets_play.config.JwtUtils;
import com._talent.lets_play.dto.AdminAuthRequest;
import com._talent.lets_play.dto.UserUpdateRequest;
import com._talent.lets_play.exception.ErrorResponse;
import com._talent.lets_play.models.*;
import com._talent.lets_play.utils.SecurityMaskingUtils;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com._talent.lets_play.services.impl.UserService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller handling authentication and user management operations.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserControllers {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final String path = "/api/auth";
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
            // Validate inputs
            if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }

            if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

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
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("username", savedUser.getName());
            response.put("role", savedUser.getRole());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception ex) {
            log.error("Error during user registration", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration");
        }
    }

    /**
     * Updates user information.
     *
     * @param userId        The ID of the user to update
     * @param updateRequest The user update information
     * @return ResponseEntity with the updated user or error message
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody UserUpdateRequest updateRequest) {
        try {
            log.info("Processing update request for user ID: {}", userId);

            // Get existing user
            User existingUser = userService.getUserbyID(userId);

            // Update user details if provided
            if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
                existingUser.setName(updateRequest.getName());
            }

            // Handle password update if provided
            if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }

            // Update user
            User updatedUser = userService.updateUser(existingUser, userId);
            log.info("User updated successfully: {}", userId);

            // Return success response without exposing password
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("email", updatedUser.getEmail());
            response.put("username", updatedUser.getName());
            response.put("role", updatedUser.getRole());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            log.warn("Update failed - user not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception ex) {
            log.error("Error updating user", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during user update");
        }
    }

    /**
     * Gets user information by ID.
     *
     * @param userId The ID of the user to retrieve
     * @return ResponseEntity with user information or error message
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        try {
            log.info("Retrieving user information for ID: {}", userId);

            User user = userService.getUserbyID(userId);

            // Return user without exposing password
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("username", user.getName());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("User retrieval failed - user not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception ex) {
            log.error("Error retrieving user information", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving user information");
        }
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId The ID of the user to delete
     * @return ResponseEntity with success message or error message
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            log.info("Processing delete request for user ID: {}", userId);

            userService.removeUser(userId);
            log.info("User deleted successfully: {}", userId);

            return ResponseEntity.ok("User deleted successfully");

        } catch (IllegalArgumentException ex) {
            log.warn("User deletion failed - user not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception ex) {
            log.error("Error deleting user", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting user");
        }
    }

    /**
     * Admin authentication endpoint.
     * Allows access with any username as long as the password is "Zone01_Dakar.sn".
     *
     * @param adminAuthRequest The admin authentication request
     * @return ResponseEntity with admin token and details or error message
     */
    @PostMapping("/admin")
    public ResponseEntity<?> adminAuthentication(@RequestBody AdminAuthRequest adminAuthRequest) {
        log.info("Admin authentication attempt for: {}", adminAuthRequest.getUsername());

        try {

            // Check if the password matches the admin password
            final String ADMIN_PASSWORD = "Zone01_Dakar.sn";
            if (!ADMIN_PASSWORD.equals(adminAuthRequest.getPassword())) {
                log.warn("Admin authentication failed - invalid password for: {}", adminAuthRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid admin credentials");
            }

            // Create or get admin user
            User adminUser;
            String username = adminAuthRequest.getUsername();

            // Check if user exists by email (username)
            Optional<User> existingUser = userService.getUserbyEmail(username);

            if (existingUser.isPresent()) {
                adminUser = existingUser.get();
                // Update role to ADMIN if it's not already
                if (!"ADMIN".equals(adminUser.getRole())) {
                    adminUser.setRole("ADMIN");
                    adminUser = userService.updateUser(adminUser, adminUser.getId());
                    log.info("User role updated to ADMIN: {}", username);
                }
            } else {
                // Create new admin user
                adminUser = new User();
                adminUser.setEmail(username);
                adminUser.setName(username);
                adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                adminUser.setRole("ADMIN");
                adminUser = userService.addUser(adminUser);
                log.info("New admin user created: {}", username);
            }

            // Create UserPrincipal manually for admin
            UserPrincipal userPrincipal = new UserPrincipal(adminUser);


            // Generate JWT token for admin
            String jwtToken = jwtUtils.generateJwtToken(userPrincipal);

            log.info("Admin authenticated successfully: {}", username);

            // Return successful response with token and admin details
            return ResponseEntity.ok(new JwtResponse(jwtToken, adminUser.getId(), adminUser.getName(), adminUser.getEmail(), userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())));

        } catch (Exception ex) {
            log.error("Error during admin authentication", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during admin authentication");
        }
    }
}

