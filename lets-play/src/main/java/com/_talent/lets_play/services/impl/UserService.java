package com._talent.lets_play.services.impl;

import com._talent.lets_play.models.User;
import com._talent.lets_play.models.UserPrincipal;
import com._talent.lets_play.repository.UserRepository;
import com._talent.lets_play.services.IUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService ,IUser  {
    private final UserRepository userRepo;

    @Override
    @Transactional
    public User addUser(User user) {
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public void removeUser(String userId) {
        userRepo.deleteById(userId);
    }

    @Override
    @Transactional
    public User updateUser(User user, String userId) {
        user.setId(userId);
        return userRepo.save(user) ;
    }

    @Override
    @Transactional
    public User getUserbyID(String userId) {
        return userRepo.findById(userId).get();
    }

    @Override
    public String roleCheck(String userId) {
        return getUserbyID(userId).getRole();
    }

    @Override
    public Optional<User> getUserbyEmail(String email) {
        return userRepo.getUsersByEmail(email);
    }

    @Override
    @Transactional
    public boolean checkPassword(String password, String userId) {
        return userRepo.findById(userId).get().getPassword().equals(password);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.getUsersByEmail(username);
        return user.map(UserPrincipal::new).orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }
}
