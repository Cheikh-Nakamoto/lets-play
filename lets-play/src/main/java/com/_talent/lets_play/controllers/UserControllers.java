package com._talent.lets_play.controllers;

import com._talent.lets_play.dto.UserUpdateRequest;
import com._talent.lets_play.exception.BadRequestException;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.models.*;
import com._talent.lets_play.services.IUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static com._talent.lets_play.utils.MakeResponse.makeresponse;

/**
 * Controller handling authentication and user management operations.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users, including CRUD operations and user profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserControllers {

    private final IUser userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users from the system.
     * Only accessible to users with an ADMIN role.
     *
     * @return List of all users with status 200 OK
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all users in the system. This endpoint requires ADMIN privileges."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Access denied\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Authentication required\" }")
                    )
            )
    })
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Received request to get all users (admin only)");
        UserDetails userDetails = getUserPrincipal();
        System.out.println(userDetails.getUsername());
        System.out.println("===================================");
        System.out.println(userDetails.getAuthorities());
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    private UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(
            summary = "Update user information",
            description = "Update user details such as username and password. Users can only update their own information unless they have ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid input data or no changes provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"No valid changes provided\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"User not found with ID: userId\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Cannot update other users' information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Access denied\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Username already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Username already exists\" }")
                    )
            )
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID of the user to update", example = "60f7b3b3b3b3b3b3b3b3b3b3")
            @PathVariable String userId,

            @Parameter(description = "User update request containing new user information")
            @Valid @RequestBody UserUpdateRequest updateRequest) {

        log.info("Processing update request for user ID: {}", userId);

        // Récupération de l'utilisateur existant
        User existingUser = userService.getUserbyID(userId);
        if (existingUser == null) {
            log.warn("Update failed - user not found: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Validation des données d'entrée
        validateUpdateRequest(updateRequest);

        // Application des modifications
        boolean hasChanges = applyUserUpdates(existingUser, updateRequest);

        if (!hasChanges) {
            log.warn("Update failed - no valid changes provided for user: {}", userId);
            throw new BadRequestException("No valid changes provided");
        }

        // Sauvegarde des modifications
        User updatedUser = userService.updateUser(existingUser, userId);
        log.info("User updated successfully: {}", userId);

        return ResponseEntity.ok((User) makeresponse(updatedUser));
    }

    /**
     * Valide la requête de mise à jour (validations métier supplémentaires)
     * Les validations @Size sont déjà gérées par @Valid dans le contrôleur
     *
     * @param updateRequest la requête de mise à jour
     */
    private void validateUpdateRequest(UserUpdateRequest updateRequest) {
        // Validation des chaînes vides (même si la longueur est valide)
        if (updateRequest.getUsername() != null && updateRequest.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty or contain only whitespace");
        }

        if (updateRequest.getPassword() != null && updateRequest.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty or contain only whitespace");
        }
    }

    /**
     * Applique les modifications à l'utilisateur existant
     *
     * @param existingUser  l'utilisateur existant
     * @param updateRequest la requête de mise à jour
     * @return true si des modifications ont été appliquées, false sinon
     */
    private boolean applyUserUpdates(User existingUser, UserUpdateRequest updateRequest) {
        boolean hasChanges = false;

        // Mise à jour du nom d'utilisateur
        if (updateRequest.getUsername() != null &&
                !updateRequest.getUsername().equals(existingUser.getName())) {

            // Vérifier l'unicité du nom d'utilisateur si nécessaire
            if (userService.existsByUsername(updateRequest.getUsername())) {
                throw new BadRequestException("Username already exists");
            }

            log.info("Updating username for user: {} from '{}' to '{}'",
                    existingUser.getId(), existingUser.getName(), updateRequest.getUsername());
            existingUser.setName(updateRequest.getUsername());
            hasChanges = true;
        }

        // Mise à jour du mot de passe
        if (updateRequest.getPassword() != null) {
            log.info("Updating password for user: {}", existingUser.getId());
            existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            hasChanges = true;
        }

        return hasChanges;
    }

    /**
     * Gets user information by ID.
     *
     * @param userId The ID of the user to retrieve
     * @return ResponseEntity with user information or error message
     */
    @GetMapping("/{userId}")
    @PostAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve user information by user ID. Users can only access their own information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"User not found\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Cannot access other users' information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Access denied\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Authentication required\" }")
                    )
            )
    })
    public ResponseEntity<?> getUserInfo(
            @Parameter(description = "ID of the user to retrieve", example = "60f7b3b3b3b3b3b3b3b3b3b3")
            @PathVariable String userId) {

        log.info("Retrieving user information for ID: {}", userId);

        User user = userService.getUserbyID(userId);
        if (user == null) {
            log.warn("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }
        user.setPassword("");
        // Return user without exposing password
        return ResponseEntity.ok(user);
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId The ID of the user to delete
     * @return ResponseEntity with a success message or error message
     */
    @DeleteMapping("/{userId}")
    @PostAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Delete user",
            description = "Delete a user account. Users can only delete their own account."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"message\": \"User deleted successfully\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"User not found\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Cannot delete other users",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Access denied\" }")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"error\": \"Authentication required\" }")
                    )
            )
    })
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID of the user to delete", example = "60f7b3b3b3b3b3b3b3b3b3b3")
            @PathVariable String userId) {

        log.info("Processing delete request for user ID: {}", userId);

        userService.removeUser(userId);
        log.info("User deleted successfully: {}", userId);

        return ResponseEntity.ok("User deleted successfully");
    }
}