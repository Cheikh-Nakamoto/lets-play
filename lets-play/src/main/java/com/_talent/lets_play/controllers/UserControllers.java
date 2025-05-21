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


    /**
     * Updates user information.
     *
     * @param userId        The ID of the user to update
     * @param updateRequest The user update information
     * @return ResponseEntity with the updated user or error message
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @Valid @RequestBody UserUpdateRequest updateRequest) {
        boolean Notfailure = false;
        log.info("Processing update request for user ID: {}", userId);
        UserPrincipal userPrincipal = getUserPrincipal();
        if (!userPrincipal.getId().equals(userId)) {
            log.warn("Update failed - user cannot update their own details");
            throw new UnauthorizedAccessException("User cannot update their own details");
        }
        // Get existing user
        User existingUser = userService.getUserbyID(userId);
        if (existingUser == null) {
            log.warn("Update failed - user not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }
        // Update user details if provided
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().trim().isEmpty()) {
            log.warn("Update failed - name cannot be empty");
            Notfailure = true;
            existingUser.setName(updateRequest.getUsername());
        }

        // Update user details if provided
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            log.warn("Update failed - name cannot be empty");
            Notfailure = true;
            existingUser.setEmail(updateRequest.getEmail());
        }


        // Handle password update if provided
        if (!updateRequest.getPassword().trim().isEmpty()) {
            log.warn("Update failed - password cannot be empty");
            Notfailure = true;
            existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        if (!Notfailure) {
            log.warn("Update failed - no changes provided");
            throw new BadRequestException("No changes provided");
        }
        // Update user
        User updatedUser = userService.updateUser(existingUser, userId);
        log.info("User updated successfully: {}", userId);

        // Return success response without exposing password


        return ResponseEntity.ok(makeresponse(updatedUser));
    }

    /**
     * Gets user information by ID.
     *
     * @param userId The ID of the user to retrieve
     * @return ResponseEntity with user information or error message
     */
    @GetMapping("/{userId}")
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
     * @return ResponseEntity with success message or error message
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        UserPrincipal userPrincipal = getUserPrincipal();
        if (!userPrincipal.getId().equals(userId) && !userPrincipal.getRole().equals("ADMIN")) {
            log.warn("Delete failed - user cannot delete their own details");
            throw new UnauthorizedAccessException("User cannot delete their own details");
        }
        log.info("Processing delete request for user ID: {}", userId);

        userService.removeUser(userId);
        log.info("User deleted successfully: {}", userId);

        return ResponseEntity.ok("User deleted successfully");
    }

}

