package com._talent.lets_play.repository;

import com._talent.lets_play.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository  extends MongoRepository<User,String> {

    Optional<User> getUsersByEmail(String email);
}
