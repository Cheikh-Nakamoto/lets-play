package com._talent.lets_play.services;

import com._talent.lets_play.models.User;

import java.util.Optional;

public interface IUser {
    User addUser(User user);
    void removeUser(String userId);
    User updateUser(User user,String userId);
    User getUserbyID(String userId);
    String roleCheck(String userId);
    Optional<User> getUserbyEmail(String email);
}
