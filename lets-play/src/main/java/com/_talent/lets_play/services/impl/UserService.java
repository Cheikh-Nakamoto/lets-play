package com._talent.lets_play.services.impl;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.models.User;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.repository.UserRepository;
import com._talent.lets_play.services.IUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing user operations.
 * Implements both UserDetailsService for Spring Security and IUser for application-specific functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService, IUser {

    private final UserRepository userRepository;
    /**
     * Retrieves all users from the system.
     *
     * @return List of all users
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users from the database");
        return userRepository.findAll();
    }


    /**
     * Adds a new user to the system.
     *
     * @param user The user entity to be added
     * @return The saved user with generated ID
     */
    @Override
    @Transactional
    public User addUser(User user) {
        log.info("Adding new user with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    /**
     * Removes a user from the system by ID.
     *
     * @param userId The ID of the user to be removed
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Override
    @Transactional
    public void removeUser(String userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to delete non-existent user with ID: {}", userId);
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }
        log.info("Removing user with ID: {}", userId);
        userRepository.deleteById(userId);
    }

    /**
     * Updates an existing user's information.
     *
     * @param user The updated user data
     * @param userId The ID of the user to update
     * @return The updated user entity
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Override
    @Transactional
    public User updateUser(User user, String userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to update non-existent user with ID: {}", userId);
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }
        user.setId(userId);
        log.info("Updating user with ID: {}", userId);
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user entity
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserbyID(String userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));
    }

    /**
     * Checks the role of a user.
     *
     * @param userId The ID of the user
     * @return The role of the user
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public String roleCheck(String userId) {
        log.debug("Checking role for user ID: {}", userId);
        return getUserbyID(userId).getRole();
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user
     * @return An Optional containing the user if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserbyEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            log.warn("Attempted to find user with null or empty email");
            return Optional.empty();
        }
        return userRepository.getUsersByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.getUsersByName(username).isPresent();
    }

    /**
     * Loads a user by username for Spring Security authentication.
     * This implementation treats the email as the username.
     *
     * @param username The username (email address) to look up
     * @return A UserDetails object for Spring Security
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Attempted authentication with null or empty username");
            throw new ResourceNotFoundException("Username cannot be empty");
        }

        log.debug("Loading user details for authentication: {}", username);
        Optional<User> user = userRepository.getUsersByEmail(username);

        return user.map(UserPrincipal::new)
                .orElseThrow(() -> {
                    log.warn("Failed authentication attempt - user not found: {}", username);
                    return new ResourceNotFoundException("User not found with email: " + username);
                });
    }
}