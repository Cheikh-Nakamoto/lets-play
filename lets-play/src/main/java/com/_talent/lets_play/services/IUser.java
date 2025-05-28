package com._talent.lets_play.services;

import com._talent.lets_play.models.User;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Optional;

public interface IUser {
    User addUser(User user);
    void removeUser(String userId);
    User updateUser(User user,String userId);
    User getUserbyID(String userId);
    String roleCheck(String userId);
    List<User> getAllUsers();
    Optional<User> getUserbyEmail(String email);

    boolean existsByUsername(@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String username);
}
