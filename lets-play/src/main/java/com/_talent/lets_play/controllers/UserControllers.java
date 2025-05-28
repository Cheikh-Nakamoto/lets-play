package com._talent.lets_play.controllers;
import com._talent.lets_play.dto.UserUpdateRequest;
import com._talent.lets_play.exception.BadRequestException;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.exception.UnauthorizedAccessException;
import com._talent.lets_play.models.*;
import com._talent.lets_play.services.IUser;
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
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Received request to get all users (admin only) ");
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
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
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
     * @param existingUser l'utilisateur existant
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
    @PostAuthorize("#userId == authentication.principal.id")  // User can only access their own data
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
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
    @PostAuthorize("#userId == authentication.principal.id")  // User can only access their own data
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        UserPrincipal userPrincipal = getUserPrincipal();
       /* if (!userPrincipal.getId().equals(userId) && !userPrincipal.getRole().equals("ADMIN")) {
            log.warn("Delete failed - user cannot delete their own details");
            throw new UnauthorizedAccessException("User cannot delete their own details");
        }*/
        log.info("Processing delete request for user ID: {}", userId);

        userService.removeUser(userId);
        log.info("User deleted successfully: {}", userId);

        return ResponseEntity.ok("User deleted successfully");
    }

}

